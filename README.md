# Aquafin Overstromingsvoorspelling Systeem

📋 **Overzicht**

Het Aquafin Overstromingsvoorspelling Systeem is een webapplicatie ontwikkeld voor het voorspellen en monitoren van overstromingsrisico's gebaseerd op neerslagdata. Het systeem helpt Aquafin techniekers en beheerders bij het proactief beheren van wateroverlast door historische data te analyseren en toekomstige neerslagpatronen te voorspellen.

## 🛠️ Installatie

Volg deze stappen om het project lokaal te installeren:

**Vereisten:**
- Java 17 of hoger
- Maven 3.6 of hoger
- IntelliJ IDEA (aanbevolen)
- MySQL Database (optioneel, gebruikt H2 in-memory database standaard)

**Installatiestappen:**

1. **Clone het project:**
```bash
git clone [(https://github.com/zaka-krc/Aquafin-case-2)]
```

2. **Project openen in IntelliJ:**
   - Open IntelliJ IDEA
   - Kies "Open" en selecteer de projectmap
   - Wacht tot Maven dependencies zijn gedownload

3. **Database configuratie (optioneel):**
```properties
# Voor MySQL (pas aan in application.properties)
spring.datasource.url=jdbc:mysql://localhost:3306/overstromingsapp_aquafin
spring.datasource.username=root
spring.datasource.password=Sabri1005!
```

4. **Applicatie starten:**
```bash
mvn spring-boot:run
```
Of gebruik de Run-configuratie in IntelliJ

5. **Toegang tot de applicatie:**
   - Open browser: `http://localhost:1070`
   - Login met: 
     - **Admin:** `admin` / `admin123`
     - **Technieker:** `tech` / `tech123`

---

## 🎯 Hoofdfunctionaliteiten

### Voor Techniekers (Gebruikers)
#### 1. **Overstromingsvoorspellingen Bekijken**
- Bekijk voorspellingen voor 2025 per maand of volledig jaar
- Analyseer risico's per seizoen (Winter, Lente, Zomer, Herfst)
- Controleer grenswaarden en risico-indicatoren
- Filter voorspellingen op specifieke maanden

#### 2. **Dashboard Monitoring**
- Real-time overzicht van huidige jaar neerslagdata
- Seizoensgebonden risico-analyses
- Historische vergelijkingen per jaar
- Trendanalyses gebaseerd op historische data

### Voor Beheerders (Admins)
#### 1. **Data Beheer**
- Handmatig neerslagdata toevoegen
- Synchroniseren met externe weer-API's
- Beheren van historische datasets
- Export en import functionaliteiten

#### 2. **Systeem Configuratie**
- Gebruikersbeheer en toegangsrechten
- API-instellingen voor weerdata
- Configuratie van risico-grenswaarden
- Systeemonderhoud en monitoring

---

## 📊 Voorspellingsmodel

### **Risico Categorieën**
Het systeem gebruikt seizoensgebonden grenswaarden:

| Seizoen | Grenswaarde (mm) | Beschrijving |
|---------|------------------|--------------|
| 🌨️ **Winter** | 300mm | December, Januari, Februari |
| 🌸 **Lente** | 250mm | Maart, April, Mei |
| ☀️ **Zomer** | 260mm | Juni, Juli, Augustus |
| 🍂 **Herfst** | 280mm | September, Oktober, November |

### **Risico Indicatoren**
- 🟢 **OK**: Neerslag onder grenswaarde
- 🔴 **GEVAAR!**: Neerslag boven kritieke grenswaarde

---

## 🔄 Data Workflow

### 1. **Data Verzameling**
- **API Integration**: Automatische sync met Belgische meteorologische dienst
- **Handmatige Invoer**: Backup voor ontbrekende data
- **Mock Data**: Realistische fallback data voor ontwikkeling

### 2. **Data Verwerking**
- **Trend Analyse**: Lineaire regressie voor toekomstvoorspellingen
- **Seizoenscorrectie**: Aanpassing gebaseerd op historische patronen
- **Risico Berekening**: Automatische vergelijking met grenswaarden

### 3. **Voorspelling Generatie**
- **Maandelijks**: Specifieke voorspellingen per maand
- **Seizoensgebonden**: Totaaloverzicht per seizoen
- **Jaarlijks**: Volledig jaaroverzicht met trends

---

## 🌐 API Integratie

### **Belgische Meteorologische Dienst**
```java
// Configuratie in WeerAPIService.java
```

**Beschikbare endpoints:**
- Historische neerslagdata per maand
- Real-time weergegevens
- Seizoensstatistieken

---

## 💡 Belangrijke Features

### **Intelligente Voorspellingen**
- Machine learning gebaseerde trendanalyse
- Seizoenscorrectie algoritmes
- Historische patroonherkenning
- Automatische risico-evaluatie

### **Flexibele Data Bronnen**
- **Real-time API**: Belgische meteorologische dienst
- **Mock Data**: Realistische testdata
- **Database Storage**: Lokale historische opslag
- **Manual Input**: Handmatige data-invoer backup

### **Gebruiksvriendelijke Interface**
- **Responsive Design**: Werkt op alle apparaten
- **Interactive Charts**: Visuele data presentatie
- **Real-time Updates**: Live data synchronisatie
- **Intuitive Navigation**: Eenvoudige gebruikerservaring

---

## 📱 Gebruikstips

### **Voor Techniekers:**
- **🔍 Dashboard Check**: Controleer dagelijks het dashboard voor actuele risico's
- **📅 Planning**: Gebruik maandvoorspellingen voor werkplanning
- **⚠️ Risico Monitoring**: Let op seizoensgebonden risico-indicatoren
- **📊 Trend Analyse**: Analyseer langetermijntrends voor strategische planning

### **Voor Admins:**
- **🔄 Data Sync**: Voer regelmatig API synchronisatie uit
- **📝 Data Validatie**: Controleer ingevoerde data op accuratesse
- **🛠️ Systeem Onderhoud**: Monitor API connecties en systeemstatus
- **📈 Performance**: Optimaliseer database prestaties regelmatig

---

## 🔐 Beveiliging & Toegang

### **Gebruikersrollen:**

#### **Technieker (ROLE_TECHNIEKER)**
- Voorspellingen bekijken
- Dashboard toegang
- Historische data raadplegen

#### **Administrator (ROLE_ADMIN)**
- Alle technieker functies
- Data management
- API configuratie
- Gebruikersbeheer
- Systeem administratie

### **Standaard Accounts:**
```
Admin Login:
- Gebruikersnaam: admin
- Wachtwoord: admin123

Technieker Login:
- Gebruikersnaam: tech  
- Wachtwoord: tech123
```

---

## 🔧 Technische Configuratie

### **Database Setup**
```sql
-- Automatische tabel creatie via JPA
-- Default H2 in-memory database
-- Optioneel: MySQL configuratie in application.properties
```

### **Mock Data vs Real API**
- **Development**: `FORCE_MOCK_DATA = true`
- **Production**: `FORCE_MOCK_DATA = false`

---

## 🚀 Aan de Slag

1. **🔐 Inloggen**: Gebruik admin of tech account
2. **📊 Dashboard**: Start met overzicht huidige risico's  
3. **🔮 Voorspellingen**: Navigeer naar voorspellingsmodule
4. **📈 Data Analyse**: Bekijk trends en patronen
5. **⚙️ Configuratie**: Pas instellingen aan (alleen admin)


## 👥 Takenverdeling

| Taak | Verantwoordelijke(n) |
|------|---------------------|
| **Backend Ontwikkeling** | Anas |
| **Frontend Templates** | Soufiane |
| **Database Design** | Sabri |
| **API Integratie** | Sabri |
| **Security Implementation** | Anas |
| **Testing & QA** | Sabri |
| **Documentatie** | Soufiane en Sabri |

---

## 📚 Gebruikte Technologieën

### **Backend Framework**
- **Spring Boot 3.x** - Application framework
- **Spring Security** - Authentication, CLaude.ai
- **Spring Data JPA** - Database abstraction
- **Spring Web** - RESTful web services
- **HTML & CSS** - CLaude.ai
- **Alle errors** - Google,CLaude.ai,Chatgpt

### **Database**
- **H2 Database** - In-memory database (development)
- **MySQL** - Production database (optional)
- **JPA/Hibernate** - Object-relational mapping

### **Frontend**
- **Thymeleaf** - Server-side templating
- **HTML/CSS** - User interface styling
- **JavaScript** - Client-side interactivity

### **Externe Integratie**
- **HTTP Client** - API communicatie
- **Jackson JSON** - JSON data processing
- **Belgische Meteorologische API** - Weerdata bron

### **Development Tools**
- **Maven** - Build automation
- **IntelliJ IDEA** - IDE
- **Git** - Version control

---

## 🔍 Troubleshooting

### **Veelvoorkomende Problemen:**

#### **API Connectie Problemen**
```java
// Check FORCE_MOCK_DATA setting in WeerAPIService.java
private static final boolean FORCE_MOCK_DATA = true;
```

#### **Database Connectie**
```properties
# Controleer application.properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.h2.console.enabled=true
```

#### **Login Problemen**
- Controleer gebruikersnaam/wachtwoord
- Check SecurityConfig.java voor user configuration
- Herstart applicatie indien nodig

---

## 📈 Toekomstige Uitbreidingen

- **📱 Mobile App**: Native mobile applicatie
- **🤖 Machine Learning**: Geavanceerde voorspellingsmodellen  
- **📧 Notifications**: Automatische waarschuwingen
- **🗺️ GIS Integration**: Geografische visualisatie
- **📊 Advanced Analytics**: Uitgebreide rapportage
- **🔌 API Gateway**: RESTful API voor externe integratie

---

**© 2024 Aquafin Overstromingsvoorspelling Systeem - Ontwikkeld voor proactief waterbeheer**
