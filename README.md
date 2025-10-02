# 💡 Система анализа энергопотребления

> **Умный помощник для осознанного энергопотребления в вашем доме.**  
> Отслеживайте использование электроэнергии по приборам, рассчитывайте расходы и получайте персонализированные рекомендации — без «умных» счётчиков и сложных интеграций.

![Java](https://img.shields.io/badge/Java-17+-ED8B00?logo=java&logoColor=white)
![JavaScript](https://img.shields.io/badge/JavaScript-Vanilla-F7DF1E?logo=javascript&logoColor=black)
![MySQL](https://img.shields.io/badge/MySQL-8.0+-4479A1?logo=mysql&logoColor=white)
![Tomcat](https://img.shields.io/badge/Tomcat-9.x-000000?logo=apache-tomcat&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-blue)

---

## 🌟 Возможности

- 🔐 **Аутентификация**: регистрация и вход с валидацией
- 📊 **Анализ по приборам**: освещение, холодильник, стиралка, ТВ, компьютер, кондиционер
- ⚡ **Расчёт потребления**: на основе мощности (Вт) и времени использования (часы/день)
- 💰 **Стоимость**: с учётом пользовательского тарифа (руб./кВт·ч)
- 📈 **Сравнение**: с средним потреблением по России (**7.5 кВт·ч/день**)
- 💡 **Рекомендации**: динамические советы по экономии энергии
- 📱 **Адаптивный дизайн**: работает на ПК и мобильных устройствах
- 🛡️ **Безопасность**: SHA-256 + соль для паролей, валидация, сессии

---

## 🛠️ Технологии

| Уровень       | Технология                     |
|---------------|-------------------------------|
| **Frontend**  | HTML, CSS, JavaScript (vanilla) |
| **Backend**   | Java Servlets (чистая Java)   |
| **База данных** | MySQL (через XAMPP)          |
| **Сервер**    | Apache Tomcat 9               |

> ✨ **Без фреймворков, без библиотек — только стандартные средства!**

---

## 🚀 Как запустить

### Требования
- JDK 11+
- Apache Tomcat 9
- XAMPP (с запущенным MySQL)
- Браузер

### Шаги

1. **Создайте базу данных**  
   В phpMyAdmin (XAMPP) выполните SQL-скрипт из [`docs/create_db.sql`](docs/create_db.sql):
   ```sql
   CREATE DATABASE energy_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   -- ... (таблицы users, appliances, tariffs)
