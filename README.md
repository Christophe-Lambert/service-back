![ArangoDB-Logo](https://www.arangodb.com/wp-content/uploads/2016/05/ArangoDB_logo_@2.png)

# Spring Data ArangoDB - Demo

This is an extensive demo on how to use [Spring Data ArangoDB](https://github.com/arangodb/spring-data) with an example dataset of **Game of Thrones** characters and locations.

## Table of Contents

* [Getting Started](#getting-started)
  * [Build a project with Maven](#build-a-project-with-maven)
  * [Create an Application class](#create-an-application-class)
  * [Create a Configuration class](#create-a-configuration-class)
* [Data modeling](#data-modeling)
* [CRUD operations](#crud-operations)
  * [Create a repository](#create-a-repository)
  * [Create a CommandLineRunner](#create-a-commandlinerunner)
  * [Save and read an entity](#save-and-read-an-entity)
  * [Run the demo](#run-the-demo)
  * [Update an entity](#update-an-entity)
  * [Save and read multiple entities](#save-and-read-multiple-entities)
  * [Read with sorting and paging](#read-with-sorting-and-paging)
* [Query by example](#query-by-example)
  * [Single entity](#single-entity)
  * [Multiple entities](#multiple-entities)
* [Derived queries](#derived-queries)
  * [Simple findBy](#simple-findby)
  * [Create an index](#create-an-index)
  * [More complex findBy](#more-complex-findby)
  * [Single entity result](#single-entity-result)
  * [countBy](#countby)
  * [removeBy](#removeby)
* [Relations](#relations)
  * [Read relations within an entity](#read-relations-within-an-entity)
  * [findBy including relations](#findby-including-relations)
* [Query methods](#query-methods)
  * [Param annotation](#param-annotation)
  * [BindVars annotation](#bindvars-annotation)
  * [QueryOptions annotation](#queryoptions-annotation)
  * [Graph traversal](#graph-traversal)
* [Geospatial queries](#geospatial-queries)
  * [Geo data modeling](#geo-data-modeling)
  * [Near](#near)
  * [Within](#within)
* [Learn more](#learn-more)


# Getting Started

## Build a project with Maven

First, we have to set up a project and add every needed dependency.
We use `Maven` and `Spring Boot` for this demo.

We have to create a Maven `pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.1.3</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>

    <groupId>com.arangodb</groupId>
    <artifactId>spring-data-arangodb-tutorial</artifactId>
    <version>1.0.0</version>

    <name>demo</name>
    <description>Demo project for Spring Boot</description>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>com.arangodb</groupId>
            <artifactId>arangodb-spring-boot-starter</artifactId>
            <version>3.1-0</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
```

## Create an Application class

After we have ensured that we can fetch all the necessary dependencies, we can create our first classes.

The `DemoApplication` class is our main class where we will later add certain `CommandLineRunner` instances to be
executed.

```java
package com.arangodb.spring.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {
    public static void main(final String... args) {
        Class<?>[] runner = new Class<?>[]{};
        System.exit(SpringApplication.exit(SpringApplication.run(runner, args)));
    }
}
```

## Application Configuration

We also need provide configuration for the database connection. 
We can do this by adding to `src/main/resources/application.properties`:

```
arangodb.spring.data.database=spring-demo
arangodb.spring.data.user=root
arangodb.spring.data.password=test
arangodb.spring.data.hosts=localhost:8529
```

# Data modeling

Let's create our first bean, which will represent a collection in our database. With the `@Document` annotation, we define
the collection as a document collection. In our case, we also define the alternative name characters for the collection.
By default, the collection name is determined by the class name. `@Document` also provides additional options for the
collection, which will be used at the creation time of the collection.

Because many operations on documents require a document handle, it's recommended to add a field of type `String`
annotated with `@Id` to every entity. The name doesn't matter. It's further recommended to **not set or change** the id
by hand.

```java
package com.arangodb.spring.demo.entity;

import com.arangodb.springframework.annotation.Document;
import org.springframework.data.annotation.Id;

@Document("characters")
public class Character {

    @Id // db document field: _key
    private String id;

    @ArangoId // db document field: _id
    private String arangoId;

    private String name;
    private String surname;
    private boolean alive;
    private Integer age;

    public Character() {
        super();
    }

    public Character(final String name, final String surname, final boolean alive) {
        super();
        this.name = name;
        this.surname = surname;
        this.alive = alive;
    }

    public Character(final String name, final String surname, final boolean alive, final Integer age) {
        super();
        this.name = name;
        this.surname = surname;
        this.alive = alive;
        this.age = age;
    }

    // getter & setter

    @Override
    public String toString() {
        return "Character [id=" + id + ", name=" + name + ", surname=" + surname + ", alive=" + alive + ", age=" + age + "]";
    }

}
```

# CRUD operations

## Create a repository

Now that we have our data model, we want to store data. For this, we create a repository interface which
extends `ArangoRepository`. This gives us access to CRUD operations, paging, and query by example mechanics.

```java
package com.arangodb.spring.demo.repository;

import com.arangodb.spring.demo.entity.Character;
import com.arangodb.springframework.repository.ArangoRepository;

public interface CharacterRepository extends ArangoRepository<Character, String> {

}
```

## Create a CommandLineRunner

To run our demo with Spring Boot we have to create a class implementing `CommandLineRunner`. In this class we can use the
`@Autowired` annotation to inject our `CharacterRepository` – we created one step earlier – and also `ArangoOperations` which
offers a central support for interactions with the database over a rich feature set. It mostly offers the features from
the [ArangoDB Java driver](https://github.com/arangodb/arangodb-java-driver) with additional exception translation.

To get the injection successfully running, we have to add `@ComponentScan` to our runner to define where Spring can find
our configuration class `DemoConfiguration`.

```java
package com.arangodb.spring.demo.runner;

import com.arangodb.spring.demo.repository.CharacterRepository;
import com.arangodb.springframework.core.ArangoOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("com.arangodb.spring.demo")
public class CrudRunner implements CommandLineRunner {

    @Autowired
    private ArangoOperations operations;
    @Autowired
    private CharacterRepository repository;

    @Override
    public void run(final String... args) throws Exception {

    }
}
```

## Save and read an entity

It's time to save our first entity in the database. Both the database and the collection don't have to be created
manually. This happens automatically as soon as we execute a database request with the components involved. We don't
have to leave the Java world to manage our database.

After we saved a character in the database the id in the original entity is updated with the one generated from the
database. We can then use this id to find our persisted entity.

```java
package com.arangodb.spring.demo.runner;

import com.arangodb.spring.demo.entity.Character;
import com.arangodb.spring.demo.repository.CharacterRepository;
import com.arangodb.springframework.core.ArangoOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.ComponentScan;

import java.util.Optional;

@ComponentScan("com.arangodb.spring.demo")
public class CrudRunner implements CommandLineRunner {

    @Autowired
    private ArangoOperations operations;
    @Autowired
    private CharacterRepository repository;

    @Override
    public void run(String... args) throws Exception {
        // first drop the database so that we can run this multiple times with the same dataset
        operations.dropDatabase();

        // save a single entity in the database
        // there is no need of creating the collection first. This happen automatically
        final Character nedStark = new Character("Ned", "Stark", true, 41);
        repository.save(nedStark);
        // the generated id from the database is set in the original entity
        System.out.println(String.format("Ned Stark saved in the database with id: '%s'", nedStark.getId()));

        // let us take a look whether we can find Ned Stark in the database
        final Optional<Character> foundNed = repository.findById(nedStark.getId());
        assert foundNed.isPresent();
        System.out.println(String.format("Found %s", foundNed.get()));
    }
}
```

## Run the demo

The last thing we have to do before we can successfully run our demo application is to add our command line runner
`CrudRunner` in the list of runners in our main class `DemoApplication`.

```java
Class<?>[]runner = new Class<?>[]{CrudRunner.class};
```

After executing the demo application, we should see the following lines within our console output. The id will of course
deviate.

```
Ned Stark saved in the database with id: '346'
Found Character [id=346, name=Ned, surname=Stark, alive=true, age=41]
```

## Update an entity

As everyone probably knows, Ned Stark died in the first season of Game of Thrones. So, we have to update his 'alive'
flag. Thanks to our `id` field in the class `Character`, we can use the `save()` method from our repository to perform
an upsert with our variable `nedStark` in which `id` is already set.

Let's add the following lines of code to the end of our `run()` method in `CrudRunner`.

```java
nedStark.setAlive(false);
repository.save(nedStark);
final Optional<Character> deadNed = repository.findById(nedStark.getId());
assert deadNed.isPresent();
System.out.println(String.format("The 'alive' flag of the persisted Ned Stark is now '%s'",deadNed.get().isAlive()));
```

If we run our demo a second time, the console output should look like this:

```
Ned Stark saved in the database with id: '508'
Found Character [id=508, name=Ned, surname=Stark, alive=true, age=41]
The 'alive' flag of the persisted Ned Stark is now 'false'
```

## Save and read multiple entities

What we can do with a single entity, we can also do with multiple entities. It's not just a single method call for
convenience purpose, it also requires only one database request.

Let's save a bunch of characters. Only main casts of Game of Thrones – but that's already a lot. After that we fetch all
of them from our collection and count them.

Extend the `run()` method with these lines of code.

```java
Collection<Character> createCharacters = createCharacters();
System.out.println(String.format("Save %s additional characters",createCharacters.size()));
repository.saveAll(createCharacters);

long count = repository.count();
System.out.println(String.format("A total of %s characters are persisted in the database", count));
```

We also need the `createCharacters()` method which looks as follow:

```java
public static Collection<Character> createCharacters(){
        return Arrays.asList(
                new Character("Robert","Baratheon",false),
                new Character("Jaime","Lannister",true,36),
                new Character("Catelyn","Stark",false,40),
                new Character("Cersei","Lannister",true,36),
                new Character("Daenerys","Targaryen",true,16),
                new Character("Jorah","Mormont",false),
                new Character("Petyr","Baelish",false),
                new Character("Viserys","Targaryen",false),
                new Character("Jon","Snow",true,16),
                new Character("Sansa","Stark",true,13),
                new Character("Arya","Stark",true,11),
                new Character("Robb","Stark",false),
                new Character("Theon","Greyjoy",true,16),
                new Character("Bran","Stark",true,10),
                new Character("Joffrey","Baratheon",false,19),
                new Character("Sandor","Clegane",true),
                new Character("Tyrion","Lannister",true,32),
                new Character("Khal","Drogo",false),
                new Character("Tywin","Lannister",false),
                new Character("Davos","Seaworth",true,49),
                new Character("Samwell","Tarly",true,17),
                new Character("Stannis","Baratheon",false),
                new Character("Melisandre",null,true),
                new Character("Margaery","Tyrell",false),
                new Character("Jeor","Mormont",false),
                new Character("Bronn",null,true),
                new Character("Varys",null,true),
                new Character("Shae",null,false),
                new Character("Talisa","Maegyr",false),
                new Character("Gendry",null,false),
                new Character("Ygritte",null,false),
                new Character("Tormund","Giantsbane",true),
                new Character("Gilly",null,true),
                new Character("Brienne","Tarth",true,32),
                new Character("Ramsay","Bolton",true),
                new Character("Ellaria","Sand",true),
                new Character("Daario","Naharis",true),
                new Character("Missandei",null,true),
                new Character("Tommen","Baratheon",true),
                new Character("Jaqen","H'ghar",true),
                new Character("Roose","Bolton",true),
                new Character("The High Sparrow",null,true));
        }
```

After executing the demo again, the console should print the following additional lines:

```
Save 42 additional characters
A total of 43 characters are persisted in the database
```


## Read with sorting and paging

Next to the normal `findAll()` method, `ArangoRepository` also offers the ability to sort the fetched entities by a given field
name. Adding the following source code at the end of your `run()` method gives you all characters sorted by field `name`.

```java
System.out.println("## Return all characters sorted by name");
List<Character> allSorted = repository.findAll(Sort.by(Sort.Direction.ASC, "name"));
allSorted.forEach(System.out::println);
```

Furthermore, it's possible to use pagination combined with sorting. With the following code you get the first 5
characters sorted by name.

```java
System.out.println("## Return the first 5 characters sorted by name");
Page<Character> first5Sorted = repository.findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "name")));
first5Sorted.forEach(System.out::println);
```

Your console output should include Arya Stark, Bran Stark, Brienne Tarth, Bronn and Catelyn Stark.

```
## Return the first 5 characters sorted by name
Character [id=1898, name=Arya, surname=Stark, alive=true, age=11]
Character [id=1901, name=Bran, surname=Stark, alive=true, age=10]
Character [id=1921, name=Brienne, surname=Tarth, alive=true, age=32]
Character [id=1913, name=Bronn, surname=null, alive=true, age=null]
Character [id=1890, name=Catelyn, surname=Stark, alive=false, age=40]
```

# Query by example

Since version 1.12, Spring Data provides the `QueryByExampleExecutor` interface which is also supported by ArangoDB
Spring Data. It allows execution of queries by `Example` instances.

Let's create a new `CommandLineRunner` for this:

```java
package com.arangodb.spring.demo.runner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

import com.arangodb.spring.demo.entity.Character;
import com.arangodb.spring.demo.repository.CharacterRepository;

@ComponentScan("com.arangodb.spring.demo")
public class ByExampleRunner implements CommandLineRunner {

    @Autowired
    private CharacterRepository repository;

    @Override
    public void run(final String... args) throws Exception {
        System.out.println("# Query by example");
    }

}
```

and add it to our `DemoApplication`.

```java
Class<?>[]runner = new Class<?>[]{
        CrudRunner.class,
        ByExampleRunner.class
};
```

## Single entity

First, we want to find Ned Stark again. But this time we don't know the id of the persisted entity. We start with
creating a Character with the same property values as the searched one. Then create an `Example` instance of it with
`Example.of(T)` and search for it with `findOne(Example)` from our `CharacterRepository`.

```java
final Character nedStark = new Character("Ned", "Stark", false, 41);
System.out.println(String.format("## Find character which exactly match %s",nedStark));
Optional<Character> foundNedStark = repository.findOne(Example.of(nedStark));
System.out.println(String.format("Found %s", foundNedStark.get()));
```

If we did everything right, the console output should be as follows:

```
# Query by example
## Find character which exactly match Character [id=null, name=Ned, surname=Stark, alive=false, age=41]
Found Character [id=1880, name=Ned, surname=Stark, alive=false, age=41]
```

## Multiple entities

Now we want to find more than one entity. During the time when I watched Game of Thrones, I saw a lot of Starks dying, so
let's take a look who is already dead. For this we create a new instance of Character with `surname` `"Stark"` and `alive`
`false`. Because we only need these two fields in our entity, we have to ignore the other fields in our `ExampleMatcher`.

```java
System.out.println("## Find all dead Starks");
Iterable<Character> allDeadStarks = repository.findAll(Example.of(new Character(null, "Stark", false)));
allDeadStarks.forEach(System.out::println);
```

After executing the application, the console output should be as follows:

```
## Find all dead Starks
Character [id=1887, name=Ned, surname=Stark, alive=false, age=41]
Character [id=1890, name=Catelyn, surname=Stark, alive=false, age=40]
Character [id=1899, name=Robb, surname=Stark, alive=false, age=null]
```

In addition to search for specific values from our example entity, we can search for dynamically depending values. In
the next example we search for a Stark who is 30 years younger than Ned Stark. Instead of changing the age for Ned Stark
in the previously fetched entity, we use a transformer within the `ExampleMatcher` and subtract 30 from the age of Ned
Stark.

```java
System.out.println("## Find all Starks which are 30 years younger than Ned Stark");
Iterable<Character> allYoungerStarks = repository.findAll(
    Example.of(foundNedStark.get(), ExampleMatcher.matchingAll()
        .withMatcher("surname", GenericPropertyMatcher::exact)
        .withIgnorePaths("id", "arangoId", "name", "alive")
        .withTransformer("age", age -> age.map(it -> (int) it - 30))));
        allYoungerStarks.forEach(System.out::println);
```

Because we are using the entity `foundNedStark` – fetched from the database – we have to ignore the field `id` which
isn't `null` in this case.

The console output should only include Arya Stark.

```
## Find all Starks which are 30 years younger than Ned Stark
Character [id=1898, name=Arya, surname=Stark, alive=true, age=11]
```

Aside from searching for exact and transformed values we can – in case of type `String` also search for other expressions.
In this last case, we search for every character whose `surname` ends with `"ark"`. The console output should include every
Stark.

```java
System.out.println("## Find all character which surname ends with 'ark' (ignore case)");
Iterable<Character> ark = repository.findAll(Example.of(new Character(null, "ark", false),
ExampleMatcher.matchingAll().withMatcher("surname", GenericPropertyMatcher::endsWith)
        .withIgnoreCase()
        .withIgnorePaths("name", "alive", "age")));
ark.forEach(System.out::println);
```

# Derived queries

Spring Data ArangoDB supports queries derived from method names by splitting it into its semantic parts and converting
into AQL. The mechanism strips the prefixes `find..By`, `get..By`, `query..By`, `read..By`, `stream..By`, `count..By`
, `exists..By`,`delete..By`, `remove..By` from the method and parses the rest. The `By` acts as a separator to indicate
the start of the criteria for the query to be built. You can define conditions on entity properties and concatenate them
with `And` and `Or`.

You can find the complete list of part types for derived queries  in the [reference documentation](https://docs.arangodb.com/stable/develop/integrations/spring-data-arangodb/reference-version-4/repositories/queries/derived-queries/).

## Simple findBy

Let's start with an easy example. We want to find characters based on their `surname`.

The only thing we have to do is to add a method `findBySurname(String)` to our `CharacterRepository` with a return type
which allows the method to return multiple instances of `Character`. For more information on which return types are
possible, see the [reference documentation](https://docs.arangodb.com/stable/develop/integrations/spring-data-arangodb/reference-version-4/repositories/queries/#return-types).

```java
public interface CharacterRepository extends ArangoRepository<Character, String> {
  Collection<Character> findBySurname(String surname);
}
```

After we extended our repository, we create a new `CommandLineRunner` and add it to our `DemoApplication`.

```java
Class<?>[]runner=new Class<?>[]{
        CrudRunner.class,
        ByExampleRunner.class,
        DerivedQueryRunner.class
};
```

In the `run()` method, we call our new method `findBySurname(String)` and try to find all characters with the `surname`
`"Lannister"`.

```java
package com.arangodb.spring.demo.runner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.ComponentScan;

import com.arangodb.spring.demo.entity.Character;
import com.arangodb.spring.demo.repository.CharacterRepository;

@ComponentScan("com.arangodb.spring.demo")
public class DerivedQueryRunner implements CommandLineRunner {

    @Autowired
    private CharacterRepository repository;

    @Override
    public void run(final String... args) throws Exception {
        System.out.println("# Derived queries");

        System.out.println("## Find all characters with surname 'Lannister'");
        Iterable<Character> lannisters = repository.findBySurname("Lannister");
        lannisters.forEach(System.out::println);
    }
}
```

After executing the demo application, we should see the following lines within our console output.

```
# Derived queries
## Find all characters with surname 'Lannister'
Character [id=238, name=Jaime, surname=Lannister, alive=true, age=36]
Character [id=240, name=Cersei, surname=Lannister, alive=true, age=36]
Character [id=253, name=Tyrion, surname=Lannister, alive=true, age=32]
Character [id=255, name=Tywin, surname=Lannister, alive=false, age=null]
```

### Create an index

Indexes allow fast access to documents, provided the indexed attribute(s) are used in a query. To make `findBySurname`
queries faster, we can create an index on the `surname` field, adding the `@PersistentIndex` to the `Character` class:

```java

@Document("characters")
@PersistentIndex(fields = {"surname"})
public class Character {
```

Next time we run our demo, the related queries will benefit from the index and avoid performing a full collection scan.

## More complex findBy

Now we're creating some methods with more parts and have a look how they fit together. Let's also use some different
return types. Again, we simply add the methods in our `CharacterRepository`.

```java
List<Character> findTop2DistinctBySurnameIgnoreCaseOrderByAgeDesc(String surname);
Collection<Character> findBySurnameEndsWithAndAgeBetweenAndNameInAllIgnoreCase(
        String suffix,
        int lowerBound,
        int upperBound,
        String[]nameList);
```

And the method calls in `DerivedMethodRunner`.

```java
System.out.println("## Find top 2 Lannnisters ordered by age");
List<Character> top2 = repository.findTop2DistinctBySurnameIgnoreCaseOrderByAgeDesc("lannister");
top2.forEach(System.out::println);

System.out.println("## Find all characters which name is 'Bran' or 'Sansa' and it's surname ends with 'ark' and are between 10 and 16 years old");
Collection<Character> youngStarks = repository.findBySurnameEndsWithAndAgeBetweenAndNameInAllIgnoreCase("ark", 10, 16, new String[]{"Bran", "Sansa"});
youngStarks.forEach(System.out::println);
```

The new methods produce the following console output:

```
## Find top 2 Lannnisters ordered by age
Character [id=444, name=Jaime, surname=Lannister, alive=true, age=36]
Character [id=446, name=Cersei, surname=Lannister, alive=true, age=36]
## Find all characters which name is 'Bran' or 'Sansa' and it's surname ends with 'ark' and are between 10 and 16 years old
Character [id=452, name=Sansa, surname=Stark, alive=true, age=13]
Character [id=456, name=Bran, surname=Stark, alive=true, age=10]
```

## Single entity result

With derived queries, we can not only query for multiple entities, but also for single entities. If we expect only a
single entity as the result, we can use the corresponding return type.

Because we have a unique persistent index on the fields `name` and `surname`, we can expect only a single entity when we query
for both.

For this example, we add the method `findByNameAndSurname(String, String)` in `CharacterRepository`, whose return type is
`Optional<Character>`.

```java
Optional<Character> findByNameAndSurname(String name, String surname);
```

And we call it from `DerivedMethodRunner`.

```java
System.out.println("## Find a single character by name & surname");
Optional<Character> tyrion = repository.findByNameAndSurname("Tyrion", "Lannister");
tyrion.ifPresent(c -> System.out.println(String.format("Found %s", c)));
```

The console output should look like this:

```
## Find a single character by name & surname
Found Character [id=974, name=Tyrion, surname=Lannister, alive=true, age=32]
```

## countBy

Aside from `findBy`, there are other prefixes supported – like `countBy`. In comparison to the previously used
`operations.collection(Character.class).count();`, the `countBy` is able to include filter conditions.

With the following lines of code, we are able to only count characters which are still alive.

`CharacterRepository`:

```java
Integer countByAliveTrue();
```

`DerivedMethodRunner`:

```java
System.out.println("## Count how many characters are still alive");
Integer alive = repository.countByAliveTrue();
System.out.println(String.format("There are %s characters still alive", alive));
```

## removeBy

The last example for derived queries is `removeBy`. Here, we remove all characters except those whose surname is ‘Stark'
and who are still alive.

`CharacterRepository`:

```java
void removeBySurnameNotLikeOrAliveFalse(String surname);
```

`DerivedMethodRunner`:

```java
System.out.println("## Remove all characters except of which surname is 'Stark' and which are still alive");
repository.removeBySurnameNotLikeOrAliveFalse("Stark");
repository.findAll().forEach(System.out::println);
```

We expect only Arya, Bran and Sansa to be left.

```
## Remove all characters except of which surname is 'Stark' and which are still alive
Character [id=1453, name=Sansa, surname=Stark, alive=true, age=13]
Character [id=1454, name=Arya, surname=Stark, alive=true, age=11]
Character [id=1457, name=Bran, surname=Stark, alive=true, age=10]
```

# Relations

Because ArangoDB as a multi-model database providing graphs as one of the key features, Spring Data ArangoDB also
supports a feature set for it.

With the `@Relations` annotation, we can define relationships between our entities. To demonstrate this we use our
previously created entity `Character`.

First, we have to add a field `Collection<Character>` children annotated
with `@Relations(edges = ChildOf.class, lazy = true)`
to `Character`.

```java
@Document("characters")
@PersistentIndex(fields = {"surname"})
public class Character {

    @Id // db document field: _key
    private String id;

    @ArangoId // db document field: _id
    private String arangoId;

    private String name;
    private String surname;
    private boolean alive;
    private Integer age;
    @Relations(edges = ChildOf.class, lazy = true)
    private Collection<Character> children;

    // ...

}
```

Then we have to create an entity for the edge we stated in `@Relations`. Other than a normal entity annotated with
`@Document`, this entity will be annotated with `@Edge`. This allows Spring Data ArangoDB to create an edge collection in
the database. Just like `Character`, `ChildOf` will also get a field for its `id`. To connect two `Character` entities,
it also gets a field of type `Character` annotated with `@From` and a field of type `Character` annotated with `@To`. `ChildOf` will be persisted in the database with the ids of these two characters.

```java
package com.arangodb.spring.demo.entity;

import com.arangodb.springframework.annotation.Edge;
import com.arangodb.springframework.annotation.From;
import com.arangodb.springframework.annotation.To;
import org.springframework.data.annotation.Id;

@Edge
public class ChildOf {

    @Id
    private String id;

    @From
    private Character child;

    @To
    private Character parent;

    public ChildOf(final Character child, final Character parent) {
        super();
        this.child = child;
        this.parent = parent;
    }

    // setter & getter

    @Override
    public String toString() {
        return "ChildOf [id=" + id + ", child=" + child + ", parent=" + parent + "]";
    }

}
```

To save instances of `ChildOf` in the database, we also create a repository for it the same way we
created `CharacterRepository`.

```java
package com.arangodb.spring.demo.repository;

import com.arangodb.spring.demo.entity.ChildOf;
import com.arangodb.springframework.repository.ArangoRepository;

public interface ChildOfRepository extends ArangoRepository<ChildOf, String> {

}
```

Now we implement another `CommandLineRunner` called `RelationsRunner` and add it to our `DemoApplication` like we did
with all the runners before.

```java
Class<?>[] runner = new Class<?>[]{
        CrudRunner.class,
        ByExampleRunner.class,
        DerivedQueryRunner.class,
        RelationsRunner.class
};
```

In the newly created `RelationsRunner`, we inject `CharacterRepository` and `ChildOfRepository` and built our relations.
First, we have to save some characters because we removed most of them within the previous chapter of this demo. To do so,
we use the static `createCharacter()` method from our `CrudRunner`. After we have successfully persisted our characters,
we want to save some relationships with our edge entity `ChildOf`. Because `ChildOf` requires instances of `Character`
with the `id` field set from the database, we first have to find them in our `CharacterRepository`. To ensure we find the
correct `Character`, we use the derived query method `findByNameAndSurename(String, String)` that gives us one
specific `Character`. Then we create instances of `ChildOf` and save them through `ChildOfRepository`.

```java
package com.arangodb.spring.demo.runner;

import com.arangodb.spring.demo.entity.ChildOf;
import com.arangodb.spring.demo.repository.CharacterRepository;
import com.arangodb.spring.demo.repository.ChildOfRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.ComponentScan;

import java.util.Arrays;

@ComponentScan("com.arangodb.spring.demo")
public class RelationsRunner implements CommandLineRunner {

    @Autowired
    private CharacterRepository characterRepo;
    @Autowired
    private ChildOfRepository childOfRepo;

    @Override
    public void run(final String... args) throws Exception {
        System.out.println("# Relations");
        characterRepo.saveAll(CrudRunner.createCharacters());
  
        // first create some relations for the Starks and Lannisters
        Character ned = characterRepo.findByNameAndSurname("Ned", "Stark").get();
        Character catelyn = characterRepo.findByNameAndSurname("Catelyn", "Stark").get();
        Character robb = characterRepo.findByNameAndSurname("Robb", "Stark").get();
        childOfRepo.saveAll(Arrays.asList(new ChildOf(robb, ned), new ChildOf(robb, catelyn)));
        Character sansa = characterRepo.findByNameAndSurname("Sansa", "Stark").get();
        childOfRepo.saveAll(Arrays.asList(new ChildOf(sansa, ned), new ChildOf(sansa, catelyn)));
        Character arya = characterRepo.findByNameAndSurname("Arya", "Stark").get();
        childOfRepo.saveAll(Arrays.asList(new ChildOf(arya, ned), new ChildOf(arya, catelyn)));
        Character bran = characterRepo.findByNameAndSurname("Bran", "Stark").get();
        childOfRepo.saveAll(Arrays.asList(new ChildOf(bran, ned), new ChildOf(bran, catelyn)));
        Character jon = characterRepo.findByNameAndSurname("Jon", "Snow").get();
        childOfRepo.save(new ChildOf(jon, ned));
  
        Character tywin = characterRepo.findByNameAndSurname("Tywin", "Lannister").get();
        Character jaime = characterRepo.findByNameAndSurname("Jaime", "Lannister").get();
        childOfRepo.save(new ChildOf(jaime, tywin));
        Character cersei = characterRepo.findByNameAndSurname("Cersei", "Lannister").get();
        childOfRepo.save(new ChildOf(cersei, tywin));
        Character joffrey = characterRepo.findByNameAndSurname("Joffrey", "Baratheon").get();
        childOfRepo.saveAll(Arrays.asList(new ChildOf(joffrey, jaime), new ChildOf(joffrey, cersei)));
        Character tyrion = characterRepo.findByNameAndSurname("Tyrion", "Lannister").get();
        childOfRepo.save(new ChildOf(tyrion, tywin));
    }
}
```

## Read relations within an entity

After we add `@Relations(edges = ChildOf.class, lazy = true) Collection<Character> children;` in `Character` we can now
load all children of a character when we fetch the character from the database. Let's again use the method
`findByNameAndSurname(String, String)` to find one specific character.

Add the following lines of code to the `run()` method of `RelationsRunner`.

```java
Character nedStark = characterRepo.findByNameAndSurname("Ned", "Stark").get();
System.out.println(String.format("## These are the children of %s:", nedStark));
nedStark.getChildren().forEach(System.out::println);
```

After executing the demo again we can see the following console output:

```
## These are the children of Character [id=2547, name=Ned, surname=Stark, alive=false, age=41]:
Character [id=2488, name=Bran, surname=Stark, alive=true, age=10]
Character [id=2485, name=Arya, surname=Stark, alive=true, age=11]
Character [id=2559, name=Robb, surname=Stark, alive=false, age=null]
Character [id=2556, name=Jon, surname=Snow, alive=true, age=16]
Character [id=2484, name=Sansa, surname=Stark, alive=true, age=13]
```

## findBy including relations

The field `children` is not persisted in the character entity itself, it is represented by the edge `ChildOf`.
Nevertheless, we can write a derived method which includes properties of all connected `Character`.

With the following two methods – added in `CharacterRepository` – we can query for `Character` which has a child with a
given `name` and `Character` which has a child in an age between two given integers.

```java
Collection<Character> findByChildrenName(String name);
Collection<Character> findByChildrenAgeBetween(int lowerBound, int upperBound);
```

Now we add a method that calls in `RelationsRunner` and search for all parents of ‘Sansa' and all parents which have a
child between 16 and 20 years old.

```
System.out.println("## These are the parents of 'Sansa'");
Iterable<Character> parentsOfSansa = characterRepo.findByChildrenName("Sansa");
parentsOfSansa.forEach(System.out::println);

System.out.println("## These parents have a child which is between 16 and 20 years old");
Iterable<Character> childrenBetween16a20 = characterRepo.findByChildrenAgeBetween(16, 20);
childrenBetween16a20.forEach(System.out::println);
```

The console output shows us that Ned and Catelyn are the parents of Sansa and that Ned, Jamie and Cersei have at least
one child in the age between 16 and 20 years.

```
## These are the parents of 'Sansa'
Character [id=2995, name=Ned, surname=Stark, alive=false, age=41]
Character [id=2998, name=Catelyn, surname=Stark, alive=false, age=40]
## These parents have a child which is between 16 and 20 years old
Character [id=2995, name=Ned, surname=Stark, alive=false, age=41]
Character [id=2997, name=Jaime, surname=Lannister, alive=true, age=36]
Character [id=2999, name=Cersei, surname=Lannister, alive=true, age=36]
```

# Query methods

We will now take a look at repository methods with self-written AQL.

When it comes to more complex use cases where a derived method would get way too long and become unreadable, queries
using [ArangoDB Query Language (AQL)](https://docs.arangodb.com/stable/aql/) can be supplied with the `@Query`
annotation on methods in our repositories.

AQL supports the usage of [bind parameters](https://docs.arangodb.com/stable/aql/fundamentals/bind-parameters/),
thus allowing to separate the query text from literal values used in the query. There are three ways of passing bind
parameters to the query in the `@Query` annotation.

## Param annotation

To pass bind parameters to our query, we can use the `@Param` annotation. With the `@Param` annotation, the argument will
be placed in the query at the place corresponding to the value passed to the `@Param` annotation.

To demonstrate this, we add another method to `CharacterRepository`:

```java
@Query("FOR c IN characters FILTER c.surname == @surname SORT c.age ASC RETURN c")
List<Character> getWithSurname(@Param("surname") String value);
```

Here, we named our bind parameter `surname` and annotated our method parameter `value` with `@Param("surname")`. Only the
value in the `@Param` annotation has to match with our bind parameter, the method parameter name does not matter.

As you can see, we used the collection name `characters` and not `character` in our query. Normally, a collection would be
named like the corresponding entity class. But as you probably remember, we used `@Document("characters")` in `Character`
which set the collection name to `characters`.

Now we create a new `CommandLineRunner` and add it to our `DemoApplication`.

```java
package com.arangodb.spring.demo.runner;

import com.arangodb.spring.demo.entity.Character;
import com.arangodb.spring.demo.repository.CharacterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

public class AQLRunner implements CommandLineRunner {

    @Autowired
    private CharacterRepository repository;

    @Override
    public void run(final String... args) throws Exception {
        System.out.println("# AQL queries");
    }

}
```

`DemoApplication`:

```java
Class<?>[]runner=new Class<?>[]{
        CrudRunner.class,
        ByExampleRunner.class,
        DerivedQueryRunner.class,
        RelationsRunner.class,
        AQLRunner.class
};
```

Add the following lines to `AQLRunner`.

```java
System.out.println("## Find all characters with surname 'Lannister' (sort by age ascending)");
List<Character> lannisters = repository.getWithSurname("Lannister");
lannisters.forEach(System.out::println);
```

The console output should give you all characters with `surname` Lannister.

```
## Find all characters with surname 'Lannister' (sort by age ascending)
Character [id=7613, name=Tywin, surname=Lannister, alive=false, age=null]
Character [id=7611, name=Tyrion, surname=Lannister, alive=true, age=32]
Character [id=7596, name=Jaime, surname=Lannister, alive=true, age=36]
Character [id=7598, name=Cersei, surname=Lannister, alive=true, age=36]
```

## BindVars annotation

In addition to number matching and the `@Param` annotation, we can use a method parameter of type `Map<String, Object>`
annotated with `@BindVars` as our bind parameters. We can then fill the map with any parameter used in the query.

Add to `CharacterRepository`:

```java
@Query("FOR c IN @@col FILTER c.surname == @surname AND c.age > @age RETURN c")
ArangoCursor<Character> getWithSurnameOlderThan(@Param("age") int value, @BindVars Map<String, Object> bindvars);
```

In this query, we used three bind parameter `@@col`, `@surname`, and `@age`. As you probably recognize, one of our bind
parameter is written with two `@`. This is a special type of bind parameter that exists for injecting collection names. This
type of bind parameter has a name prefixed with an additional `@` symbol.

Furthermore, we can see that we used `@Param` for our bind parameter `@age` but not for `@@col` and `@surname`. These
bind parameters have to be passed through the map annotated with `@BindVars`. It is also possible to use both
annotations within one query method.

The method call looks as expected. We pass an integer for the `age` bind parameter and a map with the keys `surname` and
`@col` to our new method.

```java
System.out.println("## Find all characters with surname 'Lannister' which are older than 35");
Map<String, Object> bindvars = new HashMap<>();
bindvars.put("surname", "Lannister");
bindvars.put("@col", Character.class);
ArangoCursor<Character> oldLannisters = repository.getWithSurnameOlderThan(35, bindvars);
oldLannisters.forEach(System.out::println);
```

One additional special handling for collection bind parameter is that we do not have to pass the collection name as a
String to our method. We can pass the type (`Character.class`) to our method. Spring Data ArangoDB will then determine
the collection name. This is very convenient if you have used an alternative collection name within the
`@Document` or `@Edge` annotations.

The console output should be as follows:

```
## Find all characters with surname 'Lannister' which are older than 35
Character [id=8294, name=Jaime, surname=Lannister, alive=true, age=36]
Character [id=8296, name=Cersei, surname=Lannister, alive=true, age=36]
```

## QueryOptions annotation

Sometimes, you want to be able to configure the query execution on a technical level. For this Spring Data ArangoDB
provides the `@QueryOptions` annotation. With this annotation, you are able to set something like a batch size to control
the number of results to be transferred from the database server in one roundtrip and some other things.

For our example, we want to return the number of found results. To achieve that we have to change the return type in our
previously created `getWithSurnameOlderThan(int, Map)` method from `Iterable<Character>` to `ArangoCursor<Character>`.
`ArangoCursor` provides a `getCount()` method that gives us the number of found results. But this value is only
returned from the database when we set the `count` flag in our query options to `true`, so we also have to add
the `@QueryOptions`
annotation to our method with `count = true`.

```java
@Query("FOR c IN @@col FILTER c.surname == @surname AND c.age > @age RETURN c")
@QueryOptions(count = true)
Iterable<Character> getWithSurnameOlderThan(@Param("age") int value, @BindVars Map<String, Object> bindvars);
```

Now, when we change the type of our `oldLannisters` local variable in `AQLRunner` to `ArangoCursor`, we can get the count
value from it.

```java
ArangoCursor<Character> oldLannisters = repository.getWithSurnameOlderThan(35, bindvars);
System.out.println(String.format("Found %s documents", oldLannisters.getCount()));
oldLannisters.forEach(System.out::println);
```

Our new console output should then look like this:

```
## Find all characters with surname 'Lannister' which are older than 35
Found 2 documents
Character [id=9012, name=Jaime, surname=Lannister, alive=true, age=36]
Character [id=9014, name=Cersei, surname=Lannister, alive=true, age=36]
```

## Graph traversal

Let's finish the query method topic of our demo with
a [graph traversal](https://docs.arangodb.com/stable/aql/graphs/traversals/) written in AQL where our edge
`ChildOf` is involved.

The following query searches for every `Character` connected (through `ChildOf`) with the character to whom the
passed `id` belongs to. This time we specified the edge collection within the query which we pass as a bind parameter
with the `@Param` annotation.

`CharacterRepository`:

```java
@Query("FOR v IN 1..2 INBOUND @arangoId @@edgeCol SORT v.age DESC RETURN DISTINCT v")
List<Character> getAllChildrenAndGrandchildren(@Param("arangoId") String arangoId, @Param("@edgeCol") Class<?> edgeCollection);
```

Like we did before with `Character.class` in our map, we use the type of `ChildOf` as parameter value. Because we want to
find all children and grandchildren of Tywin Lannister we first have to find him to get his `id` which we can then pass
to our query method.

`AQLRunner`:

```java
System.out.println("## Find all children and grantchildren of 'Tywin Lannister' (sort by age descending)");
List<Character> children = repository.findByNameAndSurname("Tywin", "Lannister").map(tywin ->
        repository.getAllChildrenAndGrandchildren(tywin.getArangoId(), ChildOf.class)).get();
children.forEach(System.out::println);
```

After executing the demo again we can see the following console output:

```
## Find all children and grantchildren of 'Tywin Lannister' (sort by age descending)
Character [id=11255, name=Tyrion, surname=Lannister, alive=true, age=32]
Character [id=11242, name=Cersei, surname=Lannister, alive=true, age=36]
Character [id=11253, name=Joffrey, surname=Baratheon, alive=false, age=19]
Character [id=11240, name=Jaime, surname=Lannister, alive=true, age=36]
```

# Geospatial queries

We will now take a look at geospatial queries.

Geospatial queries are a subsection of derived queries. To use a geospatial query on a collection, a geo index must
exist on that collection. A geo index can be created on a field which is a two element array, corresponding to latitude
and longitude coordinates.

As a subsection of derived queries, geospatial queries support the same return types, and also these additional three
return types: `GeoPage`, `GeoResult`, and `GeoResults`. These types must be used in order to get the distance of each
document as generated by the query.

## Geo data modeling

To demonstrate geospatial queries, we create a new entity class `Location` with a field `location` of type `org.springframework.data.geo.Point`. We
also have to create a geo index on this field. We can do so by annotating the field with `@GeoIndexed(geoJson = true)`. As you probably
remember, we have already used an index in our `Character` class, but we annotated the type and not the affected fields.
Spring Data ArangoDB offers two ways of defining an index. With `@<IndexType>Indexed` annotations, indexes for single
fields can be defined. If the index should include multiple fields, the `@<IndexType>Index` annotations can be used on
the type instead. See the
[reference documentation](https://docs.arangodb.com/stable/develop/integrations/spring-data-arangodb/reference-version-4/mapping/indexes/) for more
information.

Create a new `Location` class:

```java
package com.arangodb.spring.demo.entity;

import com.arangodb.springframework.annotation.Document;
import com.arangodb.springframework.annotation.GeoIndexed;
import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Point;

import java.util.Arrays;

@Document("locations")
public class Location {

    @Id
    private String id;
    private final String name;
    @GeoIndexed(geoJson = true)
    private final Point location;

    public Location(final String name, final Point location) {
        super();
        this.name = name;
        this.location = location;
    }

    // getter & setter

    @Override
    public String toString() {
      return "Location{" +
              "id='" + id + '\'' +
              ", name='" + name + '\'' +
              ", location=" + location +
              '}';
    }

}
```

Create the corresponding `LocationRepository` repository:

```java
package com.arangodb.spring.demo.repository;

import com.arangodb.spring.demo.entity.Location;
import com.arangodb.springframework.repository.ArangoRepository;

public interface LocationRepository extends ArangoRepository<Location, String> {

}
```

After that, we create a new `CommandLineRunner`, add it to our `DemoApplication`, and perform some insert operations with
some popular locations from Game of Thrones with the coordinates of their real counterparts.

```java
package com.arangodb.spring.demo.runner;

import com.arangodb.spring.demo.entity.Location;
import com.arangodb.spring.demo.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import java.util.Arrays;

public class GeospatialRunner implements CommandLineRunner {

    @Autowired
    private LocationRepository repository;

    @Override
    public void run(final String... args) throws Exception {
        System.out.println("# Geospatial");

        repository.saveAll(Arrays.asList(
                new Location("Dragonstone",     new Point(-6.815096, 55.167801)),
                new Location("King's Landing",  new Point(18.110189, 42.639752)),
                new Location("The Red Keep",    new Point(14.446442, 35.896447)),
                new Location("Yunkai",          new Point(-7.129532, 31.046642)),
                new Location("Astapor",         new Point(-9.774249, 31.50974)),
                new Location("Winterfell",      new Point(-5.581312, 54.368321)),
                new Location("Vaes Dothrak",    new Point(-6.096125, 54.16776)),
                new Location("Beyond the wall", new Point(-21.094093, 64.265473))
        ));
    }
}
```

`DemoApplication`:

```java
Class<?>[]runner=new Class<?>[]{
        CrudRunner.class,
        ByExampleRunner.class,
        DerivedQueryRunner.class,
        RelationsRunner.class,
        AQLRunner.class,
        GeospatialRunner.class
};
```

There are two kinds of geospatial query: `Near` and `Within`.

## Near

`Near` sorts entities by distance from a given point. The result can be restricted with paging.

`LocationRepository`:

```java
GeoPage<Location> findByLocationNear(Point location,Pageable pageable);
```

In this example, we search for locations sorted by distance to a given point which match the coordinates of Winterfell.
We use pagination to split the results in pages of five locations.

```java
System.out.println("## Find the first 5 locations near 'Winterfell'");
GeoPage<Location> first5 = repository.findByLocationNear(new Point(-5.581312, 54.368321), PageRequest.of(0, 5));
first5.forEach(System.out::println);

System.out.println("## Find the next 5 locations near 'Winterfell' (only 3 locations left)");
GeoPage<Location> next5 = repository.findByLocationNear(new Point(-5.581312, 54.368321), PageRequest.of(1, 5));
next5.forEach(System.out::println);
```

Because we used the coordinates of Winterfell, the distance in the output of Winterfell is `0`.

```
## Find the first 5 locations near 'Winterfell'
GeoResult [content: Location [id=14404, name=Yunkai, location=[31.046642, -7.129532]], distance: 3533.2076972451478 KILOMETERS, ]
GeoResult [content: Location [id=14405, name=Astapor, location=[31.50974, -9.774249]], distance: 3651.785495816579 KILOMETERS, ]
GeoResult [content: Location [id=14403, name=The Red Keep, location=[35.896447, 14.446442]], distance: 4261.971994059222 KILOMETERS, ]
GeoResult [content: Location [id=14402, name=King's Landing, location=[42.639752, 18.110189]], distance: 5074.755682897005 KILOMETERS, ]
GeoResult [content: Location [id=14407, name=Vaes Dothrak, location=[54.16776, -6.096125]], distance: 6049.156388427102 KILOMETERS, ]
## Find the next 5 locations near 'Winterfell' (only 3 locations left)
GeoResult [content: Location [id=14406, name=Winterfell, location=[54.368321, -5.581312]], distance: 6067.104268175527 KILOMETERS, ]
GeoResult [content: Location [id=14401, name=Dragonstone, location=[55.167801, -6.815096]], distance: 6165.650581599857 KILOMETERS, ]
GeoResult [content: Location [id=14408, name=Beyond the wall, location=[64.265473, -21.094093]], distance: 7350.229798961836 KILOMETERS, ]
```

## Within

`Within` both sorts and filters entities, returning those within the given distance, range, or shape.

Let's add some methods to `LocationRepository` that use different filter criteria.

```java
GeoResults<Location> findByLocationWithin(Point location,Distance distance);
Iterable<Location> findByLocationWithin(Point location,Range<Double> distanceRange);
```

With these methods, we can search for locations within a given distance or range to our point – Winterfell.

```java
System.out.println("## Find all locations within 50 kilometers of 'Winterfell'");
GeoResults<Location> findWithing50kilometers = repository
    .findByLocationWithin(new Point(-5.581312, 54.368321), new Distance(50, Metrics.KILOMETERS));
findWithing50kilometers.forEach(System.out::println);

System.out.println("## Find all locations which are 40 to 50 kilometers away from 'Winterfell'");
Iterable<Location> findByLocationWithin = repository.findByLocationWithin(new Point(-5.581312, 54.368321),
    Range.of(Range.Bound.inclusive(40000.), Range.Bound.exclusive(50000.)));
findByLocationWithin.forEach(System.out::println);
```

As you can see in our console output, both _Winterfell_ and _Vaes Dothrak_ are located within a 50 kilometers radius
around our point. But only _Vaes Dothrak_ is obviously more than 40 kilometers away from it.

```
## Find all locations within 50 kilometers of 'Winterfell'
GeoResult [content: Location [id=14843, name=Winterfell, location=[54.368321, -5.581312]], distance: 0.0 KILOMETERS, ]
GeoResult [content: Location [id=14844, name=Vaes Dothrak, location=[54.16776, -6.096125]], distance: 40.186277071065994 KILOMETERS, ]
## Find all locations which are 40 to 50 kilometers away from 'Winterfell'
Location [id=14844, name=Vaes Dothrak, location=[54.16776, -6.096125]]
```

But we can not only implement geo functions going from a single point, but it is also possible to search for locations
within a polygon.

In our last example, we add a method using `Polygon`.

`LocationRepository`:
```java
Iterable<Location> findByLocationWithin(Polygon polygon);
```

`GeospatialRunner`:
```java
System.out.println("## Find all locations within a given polygon");
Iterable<Location> withinPolygon = repository.findByLocationWithin(
        new Polygon(Arrays.asList(new Point(-25, 40), new Point(-25, 70), new Point(25, 70), new Point(-25, 40))));
withinPolygon.forEach(System.out::println);
```

The console output should be as follows:

```
## Find all locations within a given polygon
Location [id=16922, name=Beyond the wall, location=[64.265473, -21.094093]]
```

That's it! You should now have an overview of the possibilities with Spring Data ArangoDB.


# Learn more

* [ArangoDB](https://www.arangodb.com/)
* [Spring Data ArangoDB](https://github.com/arangodb/spring-data)
* [ArangoDB Java Driver](https://github.com/arangodb/arangodb-java-driver)

# Docker

docker rm $(docker ps -a -q -f status=exited)

docker sytem prune

docker build -t spring-data-demo .
docker run --network service-back_default -p 8080:8080 spring-data-demo

# Docker compose

mvn clean install 
docker sytem prune

docker-compose up --build

# Use API

http://localhost:8080/api/locations/all

http://localhost:8080/api/locations/startPositions

http://localhost:8080/api/locations/near?longitude=-5.581312&latitude=54.368321

http://localhost:8080/api/locations/within?longitude=-5.581312&latitude=54.368321&distanceKm=50

# VM Oracle
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 8080 -j ACCEPT
sudo netfilter-persistent save
sudo iptables -L -n -v


scp .\docker-compose.yml.ubuntu.v1 ubuntu@138.2.172.84:/home/ubuntu/projects/docker-compose.yml
scp .\target\*.jar ubuntu@138.2.172.84:/home/ubuntu/projects/target
scp .\Dockerfile ubuntu@138.2.172.84:/home/ubuntu/projects


créer les volumes sous Ubuntu
docker volume create arangodb_data_container
docker volume create arangodb_apps_data_container

https://www.digitalocean.com/community/tutorials/how-to-add-swap-space-on-ubuntu-20-04

https://cloud.oracle.com/?region=eu-frankfurt-1&bdcstate=default&cloudshell=true


http://138.2.172.84:8080/api/locations/all

## Github
Creer une connexion
ssh -T git@github.com