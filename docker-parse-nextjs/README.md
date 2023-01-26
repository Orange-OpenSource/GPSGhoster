# Projet 'docker-parse_nextjs'

Ce projet s'appuie sur les blogs ou projets suivants :
- [Créer une application web avec Docker, Parse et Next.js](https://www.tmocellin.com/blog/creer-une-application-web-avec-docker-parse-next-partie-1-introduction/)
- [Running a DNS Server in Docker](https://medium.com/nagoya-foundation/running-a-dns-server-in-docker-61cc2003e899)
- [How To Create a Self-Signed SSL Certificate for Nginx in Ubuntu 18.04](https://www.digitalocean.com/community/tutorials/how-to-create-a-self-signed-ssl-certificate-for-nginx-in-ubuntu-18-04)
- [Email Verification and Password Reset](https://github.com/parse-community/parse-server#email-verification-and-password-reset)
- [parse-smtp-template](https://www.npmjs.com/package/parse-smtp-template)
  
Cette application serveur utilise les technologies suivantes:
- Docker / Docker Compose
- Parse Server
- Parse Dashboard
- MongoDB
- Next (uniquement pour valider l'intégration de Parse Server) 
- un serveur DNS local au lieu de Let’s Encrypt

# Licence

Le projet 'docker-parse_nextjs'
- utilise la version 5.2.1 du projet 'parser-server' sous [licence](https://github.com/parse-community/parse-server/blob/alpha/LICENSE)
- repose sur la version 1.1.0 du projet 'parse-server-example' sous la même licence d'après l'[échange](https://github.com/parse-community/parse-server-example/issues/426)

# Prérequis 

## compte de messagerie

Une application client Android peut utiliser la librairie ***[ParseUI-Android](https://github.com/parse-community/ParseUI-Android)*** pour s'interfacer avec l'application serveur Parse.\
Dans le cas de la création d'un compte utilisateur sur le serveur Parse avec une ***vérification par e-mail***, un compte de messagerie est ***nécessaire*** pour configurer le serveur Parse.

Il est possible d'utiliser un compte de messagerie gmail.\
Dans ce cas, il est nécessaire d'activer la ***validation en deux étapes*** puis de créer une ***mot de passe d'application*** de type messagerie. \
Pour plus de détails, merci de consulter cet article. [Se connecter avec des mots de passe d'application](https://support.google.com/mail/answer/185833?hl=fr#:~:text=Cr%C3%A9er%20et%20utiliser%20des%20mots%20de%20passe%20d'application&text=Acc%C3%A9dez%20%C3%A0%20votre%20compte%20Google,devrez%20peut%2D%C3%AAtre%20vous%20connecter.). \
Il faudra ensuite utiliser le ***mot de passe d'application*** de 16 caractères au lieu du ***mot de passe du compte gmail*** afin de configurer le module parse-smpt-template dans le fichier ***parse-server/index.js***.

```javascript
   emailAdapter: {
        module: 'parse-smtp-template',
        options: {
            port : 587,
            host : "smtp.gmail.com",
            user: "<compte de messagerie>",
            password: "<mot de passe d'application>",  
            fromAddress: '<compte de messagerie>'
        }
    }
```

## Créer un certificat SSL auto-signé pour NGinx 

Ref : [How To Create a Self-Signed SSL Certificate for Nginx in Ubuntu 18.04](https://www.digitalocean.com/community/tutorials/how-to-create-a-self-signed-ssl-certificate-for-nginx-in-ubuntu-18-04)

```
$ sudo openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout ./nginx/ssl/private/nginx.key -out ./nginx/ssl/certs/nginx.crt
$ sudo openssl dhparam -out ./nginx/dhparam.pem 4096
```

## serveur DNS local (optionel)

Il est possible de specifier ses propres serveurs DNS au lieu de ceux de Google dans le fichier suivant.

```
fichier : ./dns-server/named.conf.options
    forwarders {
        8.8.8.8;
        8.8.4.4;
    };
```

## proxy (optionel)

***Dans le cas de la présence d'un proxy dans votre environnement, il pourrait être nécessaire d'appliquer le mode opératoire suivant.***

- créer le repertoire dans lequel sera stocker le fichier de configuration du service docker
```
$ mkdir -p /etc/systemd/system/docker.service.d
```
- créer les fichiers suivants avec les variables d'environnements 
```
fichier : http-proxy.conf
   [Service]
   Environment="HTTP_PROXY=http://<proxy_host>:<proxy_port>/"

fichier : https-proxy.conf
   [Service]
   Environment="HTTPS_PROXY=http://<proxy_host>:<proxy_port>/"
```
- mette à jour les modifications
```
$ sudo systemctl daemon-reload
```
- vérifier que la configuration a bien été chargée
```
$ systemctl show --property=Environnement docker
Environment="HTTP_PROXY=http://<proxy_host>:<proxy_port>/"
Environment="HTTPS_PROXY=http://<proxy_host>:<proxy_port>/"
```
- redémarrer Docker
```
$ sudo systemctl restart docker
```

- ajouter dans les fichiers ***Dockerfile*** du projet, les variables d'environnement suivantes
```
fichiers : [
  ./parse-server/Dockerfile,
  ./dashboard/Dockerfile, 
  ./dns-server/Dockerfile,
  ./web-app/Dockerfile
]
  ENV http_proxy http://<proxy_host>:<proxy_port>
  ENV https_proxy http://<proxy_host>:<proxy_port>
```

# Installation

- lister les ressources utilisées par docker  
```
  $ docker system df
```  
- forcer la libération de toutes les ressources utilisées par docker
```  
  $ docker system prune -af && docker volume prune -f
```  
- démarrer le backend
```  
  $ docker-compose up -d
```  
- ajouter avec sudo dans le fichier '/etc/resolv.conf' la ligne suivante pour utiliser le server DNS local
```
  nameserver 172.20.0.2
```  
- vérifier l'état de santé de l'api parser dans un navigateur web
```  
  https://api.gpsghoster.votre_domaine.com/health
```  
- atteindre le dashboard
```
  http://172.30.0.6:4040
```
  et s'identifier avec le compte
```  
  Username : user
  Password : password
```
-  atteindre l'application Next.js pour tester de bout en bout
```
  http://172.30.0.7:3000
```  
- arrêter le backend
```  
  $docker-compose down
```
