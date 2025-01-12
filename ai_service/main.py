import logging
from flask import Flask, request, jsonify
from pyArango.connection import Connection
import numpy as np
#from sklearn.ensemble import RandomForestClassifier  # Pour la prédiction
from sklearn.ensemble import IsolationForest  # Pour la détection d'anomalies
from sklearn.preprocessing import StandardScaler
from transformers import pipeline  # Pour le NLP avec Hugging Face
from geopy.distance import geodesic
from datetime import datetime, timedelta
import os

# Configuration du logging
logging.basicConfig(level=logging.DEBUG,  # Niveau de log (DEBUG, INFO, WARNING, ERROR, CRITICAL)
                    format='%(asctime)s - %(levelname)s - %(message)s')

app = Flask(__name__)

# Configuration de la connexion à ArangoDB
DB_HOST = os.getenv('DB_HOST', 'default_host')  # 'default_host' est une valeur par défaut si la variable n'est pas définie
DB_PORT = int(os.getenv('DB_PORT', 8529))       # Assurez-vous de convertir les ports en int
DB_USER = os.getenv('DB_USER', 'default_user')
DB_PASSWORD = os.getenv('DB_PASSWORD', 'default_password')
DB_NAME = os.getenv('DB_NAME', 'default_db')

print(f"Connecting to database {DB_NAME} on {DB_HOST}:{DB_PORT} as user {DB_USER}")

conn = Connection(arangoURL=f"http://{DB_HOST}:{DB_PORT}", username=DB_USER, password=DB_PASSWORD)
db = conn[DB_NAME]

@app.route('/health', methods=['GET'])
def health():
    # Vérifiez ici l'état de votre service si nécessaire
    return jsonify(status="healthy"), 200

# Chargement d'un modèle de machine learning pour la prédiction (exemple)
# Le modèle est supposé être déjà entraîné et sauvegardé dans `model.joblib`
#from joblib import load

scaler = StandardScaler()  # Normalisation des données

# NLP Pipeline (Hugging Face) pour transformer texte -> AQL
nlp_model = pipeline("text2text-generation", model="t5-small")  # Transformer NLP

# Charger ou entraîner le modèle de détection d'anomalies
anomaly_detector = IsolationForest(n_estimators=100, contamination=0.1, random_state=42)

@app.route('/detect-anomalies', methods=['POST'])
def detect_anomalies():
    """
    Route pour détecter les positions inhabituelles (anomalies) à partir d'une liste de positions.
    """
    try:
        # Récupérer les positions depuis la requête
        positions = request.json.get('positions')
        if not positions:
            raise ValueError("Les positions sont manquantes dans la requête.")

        logging.debug(f"Positions reçues pour détection d'anomalies : {positions}")

        # Prétraitement des données
        positions_array = np.array(positions)
        positions_scaled = scaler.fit_transform(positions_array)  # Normaliser les données

        # Détection des anomalies
        anomaly_predictions = anomaly_detector.fit_predict(positions_scaled)
        anomalies = (anomaly_predictions == -1)  # Les anomalies sont marquées par -1

        # Récupérer les positions inhabituelles
        unusual_positions = [pos for pos, is_anomaly in zip(positions, anomalies) if is_anomaly]
        logging.info(f"Anomalies détectées : {len(unusual_positions)} positions inhabituelles.")

        # Retourner les anomalies
        return jsonify({
            "unusual_positions": unusual_positions,
            "count": len(unusual_positions)
        })

    except Exception as e:
        logging.error(f"Erreur lors de la détection d'anomalies : {str(e)}")
        return jsonify({"error": str(e)}), 500


# Paramètres d'anomalie
distance_threshold = 500  # Distance en mètres
time_threshold = timedelta(minutes=30)  # Changement de position anormal après 30 minutes

def detect_anomalies(data):
    anomalies = []
    for i in range(1, len(data)):
        prev_point = data[i-1]
        curr_point = data[i]

        # Convertir les timestamps en objets datetime
        prev_time = datetime.strptime(prev_point['timestamp'], "%Y-%m-%dT%H:%M:%S")
        curr_time = datetime.strptime(curr_point['timestamp'], "%Y-%m-%dT%H:%M:%S")

        # Calculer la distance géographique entre les deux points
        prev_coords = (prev_point['lat'], prev_point['lng'])
        curr_coords = (curr_point['lat'], curr_point['lng'])
        distance = geodesic(prev_coords, curr_coords).meters

        # Vérifier l'anomalie basée sur la distance et le temps
        if distance > distance_threshold and (curr_time - prev_time) > time_threshold:
            anomalies.append(curr_point)

    return anomalies

# Fonction pour vérifier l'anomalie
@app.route('/detect-temporal-anomalies', methods=['POST'])
def detect_temporal_anomalies():
    try:
        # Récupérer les données du corps de la requête
        data = request.json
        # Logique de détection d'anomalies temporelles (à implémenter ici)
        anomalies = detect_anomalies(data)  # Appel à une fonction de détection

        # Retourner les anomalies détectées
        return jsonify({"anomalies": anomalies})

    except Exception as e:
        logging.error(f"Erreur dans la détection des anomalies temporelles : {str(e)}")
        return jsonify({"error": str(e)}), 500

@app.route('/nlp-to-aql', methods=['POST'])
def nlp_to_aql():
    """
    Route pour convertir une phrase en langage naturel en requête AQL.
    """
    try:
        # Récupérer la phrase en langage naturel
        natural_query = request.json.get('query')
        if not natural_query:
            raise ValueError("La requête en langage naturel est manquante.")

        logging.debug(f"Phrase en langage naturel reçue : {natural_query}")

        # Générer une requête AQL à l'aide du modèle NLP
        aql_query = nlp_model(f"Convert: {natural_query}")[0]['generated_text']
        logging.info(f"Requête AQL générée : {aql_query}")

        return jsonify({"aql_query": aql_query})

    except Exception as e:
        logging.error(f"Erreur lors de la conversion NLP -> AQL : {str(e)}")
        return jsonify({"error": str(e)}), 500

@app.route('/analyze', methods=['POST'])
def analyze_positions():
    # Log de la requête reçue
    logging.info("Requête reçue pour l'analyse des positions.")

    try:
        # Récupérer la requête AQL brute directement à partir du corps de la requête
        query = request.json.get('query', 'FOR p IN locations RETURN p')
        logging.debug(f"Requête AQL reçue : {query}")

        # Exécuter la requête ArangoDB
        result = db.AQLQuery(query, rawResults=True)
        data = [item for item in result]
        logging.info(f"Résultats de la requête ArangoDB récupérés : {len(data)} éléments.")

        # Exemple d'analyse : calcul de barycentre
        positions = np.array([d['location']['coordinates'] for d in data])
        center = positions.mean(axis=0).tolist()
        logging.debug(f"Barycentre calculé : {center}")

        # Retourner le barycentre
        return jsonify({"barycenter": {"longitude": center[0], "latitude": center[1]}})

    except Exception as e:
        # Log de l'erreur
        logging.error(f"Erreur lors de l'exécution de la requête AQL : {str(e)}")
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)

