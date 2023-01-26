# Projet 'gpsghoster'

Ce projet s'appuie sur le projet ***docker-parse_nextjs*** qui fournit un backend avec les services suivants :
- enregistrement et authentification d'un compte utilisateur à créer sur le backend,
- sauvegarde et restauration de données de l'application Android.

L'application Android est composée de 3 modules :
- ***login*** \
  Ce module est une version légèrement modifiée du module ***login*** du projet [ParseUI-Android](https://github.com/parse-community/ParseUI-Android) .\
  Il intervient dans les usages d'enregistrement et d'authentification d'un compte utilisateur.
- ***common*** \
  Ce module intègre plusieurs codes réutilisables parmi :
  - la gestion des permissions Android 
  - la gestion des préférences Android avec leur chiffrement
  - l'usage de la biométrie en s'appuyant sur le blog [Android Biometric API: Getting Started](https://www.kodeco.com/18782293-android-biometric-api-getting-started)
- ***app*** \
  Ce module implémente un algorithme d'anonymisation de la position GPS fournit par un smartphone Android en combinant :
  - un algorithme de dissimulation des positions les plus sensibles avec des zones définies en utilisant les données d'OpenStreetMap
  - un algorithme d'indiscernabilité des positions les moins sensibles

  L'application permet de :
  - créer ou supprimer une zone sensible sur une carte en exploitant les données d'OpenStreetMap disponibles dans un carré de 1km de coté et centrée sur la position sensible
  - enregistrer les zones sensibles dans le backend afin de les récupérer lors de la prochaine reconnexion de l'utilisateur
  - configurer les fournisseurs de position GPS de la plateforme Android
  - activer ou désactiver l'anonymisation
  - enregistrer des jeux de données d'anonymisation dans le backend afin de les analyser et proposer d'éventuelles améliorations de l'algorithme  
    
# Prérequis

## obligatoire

- créer un magasin de clé contenant la clé de signature pour l'application Android.
```
$ keytool -genkey -v -keystore ./gpsGhoster.keystore -alias gpsGhoster -keyalg RSA -keysize 4096 -validity 10000
```

Lors de la création de gpsGhoster.keystore utiliser les paramètres par default suivants et définis dans le fichier 'app.properties' :
```
keystore=gpsGhoster.keystore
keystore.password=gpsGhoster
keyAlias=gpsGhoster
keyPassword=gpsGhoster
```

- mettre à jour l'url pour atteindre le serveur parse si elle est différente de 'api.gpsghoster.votre_domaine.com'

```
./gpsghoster/app/src/release/res/values/strings.xml:13:    <string name="parse_server">https://api.gpsghoster.votre_domaine.com</string>
./gpsghoster/app/src/release/res/values/strings.xml:14:    <string name="parse_server_hostname">api.gpsghoster.votre_domaine.com</string>
./gpsghoster/app/src/main/res/xml/network_security_config.xml:12:        <domain includeSubdomains="true">api.gpsghoster.votre_domaine.com</domain>
./gpsghoster/app/src/main/res/values/strings.xml:14:    <string name="parse_server_hostname">api.gpsghoster.votre_domaine.com</string>
```

- recopier le certificat du serveur 'api.gpsghoster.votre_domaine.com'
de   ./../docker-parse-nextjs/nginx/ssl/certs/nginx.crt (cf ./../docker-parse-nextjs/README.md § 'Créer un certificat SSL auto-signé pour NGinx')
vers ./gpsghoster/app/src/main/res/raw/certificate

- créer un Android Virtual Device 'Pixel_4_API_31' à partir d'Android studio et le 'Device Manager'

- démarrer l'émulateur en ligne de commande au lieu d'utiliser l'appel à Android Studio \
  ***Attention***, 
  - ajouter le chemin .../android-sdk/emulator dans la variable d'environnement PATH afin de trouver l'executable emulator.
  - l'émulateur lancé avec android Studio ne gère pas correctement la simulation de la position ou la simulation d'un parcours du smartphone
```
  $ emulator -avd Pixel_4_API_31 -no-snapshot-load
```
- ajouter un compte Google avec un e-mail valide à utiliser ensuite dans l'application GpsGhoster pour valider le profil sur le backend.
```
  > Settings
```
- autoriser le mode développeur
```
  > Settings / About emulated device 
  > faire 7 clicks sur 'Build number' 
```
- installer l'application GpsGhoster avec Android Studio par exemple et autoriser la à modifier la position gps.
```
  > Settings / System / Developer options / Location - Select mock location app 
```  
- ajouter un code PIN '0000' associé à l'empreinte 'Finger 1' de l'émulateur
```  
   > Settings / Security / Pixel Imprint 
```
- vérifier l'accès au backend avec https://api.gpsghoster.votre_domaine.com/health qui retourne un status OK

- créer des positions et des routes dans l'émulateur à partir du menu 'Settings / Location' puis 'Singe points' ou 'Routes'. \
  ***Attention***, pour importer un parcours dans l'émulateur créer le à partir de 'Google My Maps' puis exporter le au format KML. 
```
  https://www.google.com/maps/d/
```

## optionel
- configurer le device émulé avec l'accès au réseau & internet via le proxy de l'envionnement et pour accéder au backend local.
```
  > Settings / Network & internet / Internet / Android Wifi
  > Proxy hostname : <proxy_host>
  > Proxy port : <proxy_port>
  > Bypass proxy for : 172.20.0.2, 172.30.0.5, api.gpsghoster.votre_domaine.com
```

# Installation

- installer l'application GpsGhoster avec Android Studio par exemple et autoriser la à modifier la position gps.
```
  > Settings / System / Developer options / Location - Select mock location app 
```  
