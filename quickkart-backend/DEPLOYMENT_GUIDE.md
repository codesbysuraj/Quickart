# QuickKart Deployment Guide

## Step 1: Set Up Database in DBeaver

### 1.1 Install DBeaver
- Download from: https://dbeaver.io/download/
- Install and launch DBeaver

### 1.2 Create MySQL Connection
1. Click "New Database Connection" (plug icon)
2. Select "MySQL"
3. Configure connection:
   - Host: `localhost`
   - Port: `3306`
   - Database: `quickkart_db`
   - Username: `root`
   - Password: (your MySQL password)
4. Click "Test Connection" to verify
5. Click "Finish"

### 1.3 Run Database Schema
1. Open `schema.sql` file in DBeaver
2. Execute the entire script (Ctrl+Enter or click Execute button)
3. This will create:
   - Database: `quickkart_db`
   - Tables: `users`, `products`, `cart`, `orders`, `order_items`, `addresses`, `notifications`

### 1.4 Verify Tables Created
```sql
USE quickkart_db;
SHOW TABLES;
```

## Step 2: Update Existing Passwords (IMPORTANT!)

### 2.1 Update application.properties
Make sure your `backend/src/main/resources/application.properties` has:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/quickkart_db
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
spring.jpa.hibernate.ddl-auto=update
```

### 2.2 Build and Run Backend
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

The compile errors will be resolved once you run `mvn clean install` as it will download the Spring Security dependency.

### 2.3 Existing Users Note
⚠️ **IMPORTANT**: After deploying password encryption:
- Old users with plain-text passwords **will not be able to login**
- They need to **re-register** with new accounts
- OR you can manually update their passwords in the database

To manually update a password in DBeaver:
```sql
-- Example: Update password for user 'john'
-- BCrypt hash of 'password123'
UPDATE users 
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMy.uUsIZEzwKz1Z3VyqgX6C/zzWu3C6wzu'
WHERE username = 'john';
```

## Step 3: Deploy Frontend

### 3.1 Test Locally First
1. Make sure backend is running on `http://localhost:8080`
2. Open `frontend/index.html` in browser
3. Test registration, login, and all features

### 3.2 Deployment Options

#### Option A: GitHub Pages (Free - Frontend Only)
1. Create GitHub repository
2. Push frontend files to `gh-pages` branch
3. Enable GitHub Pages in repository settings
4. Update `backend-integration.js` to point to your live backend API

#### Option B: Netlify/Vercel (Free - Frontend)
1. Create account on Netlify or Vercel
2. Connect your GitHub repository
3. Set build settings:
   - Build command: (none needed)
   - Publish directory: `frontend`
4. Deploy

#### Option C: Full Stack on Cloud

**For Backend (Spring Boot):**
- **Heroku**: https://www.heroku.com/
- **Railway**: https://railway.app/
- **Render**: https://render.com/
- **AWS Elastic Beanstalk**

**For Frontend:**
- Same as above (GitHub Pages/Netlify/Vercel)

**For Database:**
- **ClearDB** (Heroku addon)
- **PlanetScale** (Free MySQL)
- **AWS RDS** (MySQL)

## Step 4: Environment Configuration

### 4.1 Update API_BASE_URL
In `frontend/backend-integration.js`, change:
```javascript
const API_BASE_URL = 'YOUR_LIVE_BACKEND_URL/api';
```

### 4.2 Update CORS in Backend
In `backend/src/main/java/com/quickkart/config/CorsConfig.java`:
```java
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
            .allowedOrigins("YOUR_FRONTEND_URL") // Add your frontend domain
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*");
}
```

## Step 5: Production Checklist

- [ ] Database schema created in DBeaver
- [ ] Password encryption enabled (BCrypt)
- [ ] All existing users re-registered or passwords updated
- [ ] Backend running and accessible
- [ ] Frontend pointing to correct backend URL
- [ ] CORS configured for production domain
- [ ] SSL/HTTPS enabled (recommended)
- [ ] Environment variables secured
- [ ] Database backups configured

## Recommended Free Deployment Stack

1. **Backend**: Railway.app or Render.com (Free tier)
2. **Frontend**: Netlify or Vercel (Free)
3. **Database**: PlanetScale (Free MySQL 5GB)

## Quick Deploy Commands

### Build Backend JAR
```bash
cd backend
mvn clean package
```

### Deploy to Railway (Example)
1. Install Railway CLI: `npm i -g @railway/cli`
2. Login: `railway login`
3. Initialize: `railway init`
4. Deploy: `railway up`

## Support
For issues, check:
- Backend logs
- Browser console (F12)
- Database connection in DBeaver
- CORS errors in network tab
