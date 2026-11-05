# J2E-TP8 — Spring `@RestController`

Implémentation du **TP 8** du cours *Architecture Microservices : Conception, Déploiement et Orchestration* (MLIAEdu).

API REST de comptes bancaires construite avec **Spring MVC (`@RestController`)**,
capable de répondre en **JSON et en XML**, documentée avec **Swagger** et consommable
depuis le client React du TP 9.

> Le code, les commentaires et les noms enregistrés en base (table `accounts`, colonnes
> `balance` / `creation_date` / `type`) sont en **anglais**. Seule cette documentation est en français.

## Objectifs couverts

| Étape du TP | Où |
|---|---|
| 1. Projet Spring Initializr | `pom.xml` |
| 2. Configuration H2 | `src/main/resources/application.properties` |
| 3. Entité JPA | `entities/Account.java` |
| 4. Énumération `TypeCompte` | `entities/AccountType.java` |
| 5. Repository JPA | `repositories/AccountRepository.java` |
| 6. Initialisation des données | `config/DataInitializer.java` |
| 7. Service REST `@RestController` | `controllers/AccountController.java` |
| 8. Intégration Swagger | `config/OpenApiConfig.java` + springdoc |
| 9. Support JSON + XML | `jackson-dataformat-xml` + `@JacksonXmlRootElement` |
| 10. Lancement | `mvn spring-boot:run` |
| 11. Tests Postman / Curl / SoapUI | section [Exemples](#exemples) |
| 12. À retenir | section [Points clés](#points-clés) |

## Structure

```
J2E-TP8-RestController/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/ma/mlia/banque/
    │   │   ├── MsBanqueApplication.java
    │   │   ├── entities/
    │   │   │   ├── Account.java        # @Entity + @JacksonXmlRootElement
    │   │   │   └── AccountType.java    # CURRENT, SAVINGS
    │   │   ├── repositories/AccountRepository.java
    │   │   ├── controllers/AccountController.java
    │   │   └── config/
    │   │       ├── OpenApiConfig.java
    │   │       ├── CorsConfig.java
    │   │       └── DataInitializer.java
    │   └── resources/application.properties
    └── test/
        ├── java/ma/mlia/banque/AccountControllerTest.java
        └── resources/application-test.properties
```

## Modèle de données

```
accounts
--------
id             BIGINT (PK, auto-increment)
balance        DOUBLE
creation_date  DATE
type           VARCHAR(255)  -- CURRENT | SAVINGS
```

## Prérequis

- JDK 17+
- Maven 3.8+
- Aucune base externe : **H2 en mémoire**

## Exécution

```bash
mvn spring-boot:run     # http://localhost:8082
mvn test
```

- Console H2 : <http://localhost:8082/h2-console> (`jdbc:h2:mem:banque`, user `sa`)
- Swagger UI : <http://localhost:8082/swagger-ui.html>
- OpenAPI JSON : <http://localhost:8082/v3/api-docs>

## Endpoints

| Méthode | URL | Statut | Description |
|---|---|---|---|
| GET | `/banque/comptes` | 200 | Tous les comptes (JSON/XML) |
| GET | `/banque/comptes/{id}` | 200 / 404 | Un compte |
| POST | `/banque/comptes` | 201 + `Location` | Créer un compte |
| PUT | `/banque/comptes/{id}` | 200 / 404 | Mettre à jour |
| DELETE | `/banque/comptes/{id}` | 200 / 404 | Supprimer |
| GET | `/banque/comptes/type?type=SAVINGS` | 200 | Filtrer par type |

### Exemples

```bash
# Liste en JSON
curl -X GET "http://localhost:8082/banque/comptes" -H "Accept: application/json"

# Liste en XML
curl -X GET "http://localhost:8082/banque/comptes" -H "Accept: application/xml"

# Création JSON
curl -X POST "http://localhost:8082/banque/comptes" \
  -H "Content-Type: application/json" \
  -d '{"balance":1500.0,"creationDate":"2024-11-01","type":"CURRENT"}'

# Création XML
curl -X POST "http://localhost:8082/banque/comptes" \
  -H "Content-Type: application/xml" \
  -d '<account><balance>1500.0</balance><creationDate>2024-11-01</creationDate><type>CURRENT</type></account>'

# Mise à jour
curl -X PUT "http://localhost:8082/banque/comptes/1" \
  -H "Content-Type: application/json" \
  -d '{"balance":2000.0,"creationDate":"2024-11-01","type":"SAVINGS"}'

# Suppression
curl -X DELETE "http://localhost:8082/banque/comptes/1"
```

Codes renvoyés, vérifiés manuellement sur l'application démarrée :

```
GET    /banque/comptes        -> 200 (JSON et XML)
POST   /banque/comptes        -> 201, Location: /banque/comptes/4
GET    /banque/comptes/4      -> 200
GET    /banque/comptes/9999   -> 404
PUT    /banque/comptes/4      -> 200
DELETE /banque/comptes/4      -> 200
```

## Points clés

- **`@RestController`** = `@Controller` + `@ResponseBody` : chaque méthode renvoie directement
  l'objet sérialisé dans le corps de la réponse.
- **`ResponseEntity`** permet de maîtriser le statut, les en-têtes et le corps.
  - `ResponseEntity.ok().body(x)` → 200
  - `ResponseEntity.notFound().build()` → 404
  - `ResponseEntity.created(location).body(x)` → 201 + en-tête `Location`
- **JSON + XML** : `produces` / `consumes` sur les mappings, plus la dépendance
  `jackson-dataformat-xml`. Spring enregistre alors `MappingJackson2XmlHttpMessageConverter`.
- **Injection par constructeur** partout.
- **`@Profile("!test")`** sur `DataInitializer` pour ne pas polluer les tests.

## Tests

`AccountControllerTest` lance l'application (`RANDOM_PORT`) et interroge réellement les endpoints :

| Test | Vérifie |
|---|---|
| `getAllAccountsShouldReturnJson` | corps JSON |
| `getAllAccountsShouldReturnXmlWhenRequested` | corps XML (`<List><item>…`) |
| `createAccountShouldReturnCreatedAndALocationHeader` | 201 + `Location` |
| `createAccountShouldAcceptXmlPayload` | payload XML accepté |
| `getAccountByIdShouldReturnNotFoundForAnUnknownId` | 404 |
| `updateAccountShouldReplaceTheBalance` | solde et type mis à jour |
| `deleteAccountShouldRemoveIt` | suppression effective |
| `getAccountsByTypeShouldFilterOnTheEnum` | filtre sur l'énumération |

```
Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
```

## Lien avec le TP 9

`CorsConfig` autorise `http://localhost:*`. Le client React du TP 9
(`J2E-TP9-React-Client`) consomme directement cette API sur le port 8082.
