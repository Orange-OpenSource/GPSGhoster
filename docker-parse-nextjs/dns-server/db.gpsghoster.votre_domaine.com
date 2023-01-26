$TTL    604800
@       IN      SOA     ns1.gpsghoster.votre_domaine.com. root.gpsghoster.votre_domaine.com. (
                  3     ; Serial
             604800     ; Refresh
              86400     ; Retry
            2419200     ; Expire
             604800 )   ; Negative Cache TTL
;
; name servers - NS records
@     IN      NS      ns1.gpsghoster.votre_domaine.com.

; name servers - A records
ns1.gpsghoster.votre_domaine.com.            IN      A      172.20.0.2

api.gpsghoster.votre_domaine.com.            IN      A      172.20.0.3
;dashboard.gpsghoster.votre_domaine.com.      IN      A      172.20.0.3
;webapp.gpsghoster.votre_domaine.com.         IN      A      172.20.0.3
