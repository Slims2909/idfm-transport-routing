---

## Interface Utilisateur – JavaFX

Cette branche contient l'interface graphique développée en JavaFX pour le projet de Génie Logiciel Avancé.

### 📁 Structure

- `src/main/java` : Code source Java
- `src/main/resources` : Fichiers de données (plan, adresses) et icônes
- `src/test/java` : Tests unitaires (ex: `AppTest.java`)
- `pom.xml` : Configuration Maven

### 🚀 Lancer l'application

Assurez-vous d’avoir Java 11+ et Maven installés.

```bash
mvn clean install
mvn exec:java -Dexec.mainClass="com.nissrine.itineraire.MainFX"
```

### 🧪 Exécuter les tests
Pour lancer les tests unitaires Maven (par exemple AppTest.java) :

```bash
mvn test
```

### ✅ Prérequis
Java JDK 11 ou supérieur
Maven 3.6+
OpenJFX (géré automatiquement via pom.xml)

### 📸 Aperçu
L'interface permet de visualiser un itinéraire, lancer une simulation, et afficher un plan interactif avec icônes.

Développé par Nissrine ELABJANI

