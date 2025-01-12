from sklearn.ensemble import RandomForestClassifier
from joblib import dump

# Exemple de données factices pour entraîner un modèle
X_train = [[0, 0], [1, 1], [0.5, 0.5], [0, 1], [1, 0]]
y_train = [0, 1, 0, 1, 0]

# Entraînement du modèle
model = RandomForestClassifier(n_estimators=10, random_state=42)
model.fit(X_train, y_train)

# Sauvegarde du modèle
dump(model, 'model.joblib')
print("Modèle sauvegardé sous 'model.joblib'")
