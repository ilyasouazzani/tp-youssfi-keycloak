# Keycloak 19 et validation des rôles
Le fichier realm-enset.json importe le realm, les deux utilisateurs et les clients de démonstration. À la racine du dépôt, démarrer Keycloak 19.0.3 avec Docker Desktop lancé.

    $env:KEYCLOAK_ADMIN = 'tp-admin'
    $env:KEYCLOAK_ADMIN_PASSWORD = [guid]::NewGuid().ToString('N')
    docker run --rm --name enset-keycloak -p 127.0.0.1:8180:8080 -e KEYCLOAK_ADMIN -e KEYCLOAK_ADMIN_PASSWORD --mount "type=bind,source=$(Join-Path (Get-Location) 'realm-enset.json'),target=/opt/keycloak/data/import/realm-enset.json,readonly" quay.io/keycloak/keycloak:19.0.3 start-dev --import-realm

Dans la console locale http://localhost:8180 ouvrir le realm enset et définir un mot de passe de démonstration non temporaire pour chaque utilisateur. Attribuer le rôle lecteur au compte de service du client enset-api. Consulter le secret du client dans Credentials. Compiler avec mvn package, puis lancer java -jar target/api-securisee-1.0.0.jar.

Dans Postman, envoyer un formulaire POST à http://localhost:8180/realms/enset/protocol/openid-connect/token avec les variantes suivantes.

| Essai | Paramètres de formulaire |
|---|---|
| Utilisateur | grant_type=password, client_id=enset-tp, username, password |
| Renouvellement | grant_type=refresh_token, client_id=enset-tp, refresh_token |
| Service | grant_type=client_credentials, client_id=enset-api, client_secret |

Analyser les claims localement. L’access token permet d’appeler l’API jusqu’à exp et porte notamment iss, aud et realm_access. Le refresh token se transmet au serveur d’identité pour obtenir un access token neuf. Faire varier Access Token Lifespan puis comparer exp moins iat sur les nouveaux access tokens. Contrôler la durée de session et les paramètres de rotation pour l’usage du refresh token.

| Contrôle API | Résultat attendu |
|---|---|
| GET http://localhost:8083/public sans token | 200 |
| GET http://localhost:8083/me sans token | 401 |
| GET http://localhost:8083/me avec le lecteur | 200 |
| GET http://localhost:8083/admin avec le lecteur | 403 |
| GET http://localhost:8083/admin avec le gestionnaire | 200 |

Les résultats ci-dessus sont des résultats attendus. Faire les captures Postman après exécution en masquant les secrets et les tokens. Le flux mot de passe est réservé à l’exercice local demandé. Pour un frontend Angular, utiliser un client public distinct en Authorization Code avec PKCE S256, une URI de redirection locale exacte, et transmettre l’access token en Bearer. Le frontend Angular et l’intégration Keycloak avec toutes les routes du projet sont à compléter.

Source : https://www.keycloak.org/docs/latest/server_admin/index.html

