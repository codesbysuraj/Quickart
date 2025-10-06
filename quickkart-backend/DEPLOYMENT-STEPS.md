# 🚀 QuickKart Live Deployment Guide

## Overview
This guide will help you deploy QuickKart to production using:
- **Railway** - Backend + MySQL Database (Free Tier)
- **Netlify** - Frontend Static Hosting (Free Tier)

---

## Step 1: Deploy Backend to Railway

### 1.1 Sign Up for Railway
1. Go to https://railway.app/
2. Click "Login" and sign up with GitHub
3. Verify your account (you get $5 free credit)

### 1.2 Create New Project
1. Click "New Project"
2. Select "Deploy from GitHub repo"
3. Connect your GitHub account
4. Create a new repository called `quickkart-backend`

### 1.3 Push Backend Code to GitHub
Open terminal and run these commands:

```bash
cd quickkart-backend/backend
git init
git add .
git commit -m "Initial commit - QuickKart Backend"
git remote add origin https://github.com/YOUR_USERNAME/quickkart-backend.git
git branch -M main
git push -u origin main
```

### 1.4 Add MySQL Database on Railway
1. In your Railway project, click "+ New"
2. Select "Database" → "Add MySQL"
3. Railway will create a MySQL database and provide credentials

### 1.5 Configure Environment Variables
In Railway, go to your backend service → Variables tab, add:

```
SPRING_DATASOURCE_URL=mysql://${MYSQLHOST}:${MYSQLPORT}/${MYSQLDATABASE}
SPRING_DATASOURCE_USERNAME=${MYSQLUSER}
SPRING_DATASOURCE_PASSWORD=${MYSQLPASSWORD}
FRONTEND_URL=https://YOUR_NETLIFY_SITE.netlify.app
```

Railway will automatically replace the ${} variables with your MySQL credentials.

### 1.6 Deploy Backend
1. Select your GitHub repository in Railway
2. Railway will automatically detect Spring Boot and deploy
3. Click "Generate Domain" to get your backend URL
4. Copy this URL (e.g., `https://quickkart-backend-production.up.railway.app`)

---

## Step 2: Set Up Database Schema

### 2.1 Connect to Railway MySQL
1. In Railway, click on your MySQL database
2. Go to "Connect" tab
3. Copy the MySQL connection URL

### 2.2 Execute Schema SQL
You have two options:

**Option A: Using MySQL Workbench**
1. Download MySQL Workbench
2. Create new connection with Railway credentials
3. Open and execute `schema.sql`

**Option B: Using Railway CLI**
```bash
railway login
railway link
railway connect MySQL
```
Then paste and execute the contents of `schema.sql`

---

## Step 3: Update Backend CORS Settings

The backend needs to allow requests from your Netlify frontend URL.

Update `src/main/java/com/quickkart/config/CorsConfig.java`:
- Change allowed origins from `"*"` to your Netlify URL
- Example: `allowedOrigins("https://quickkart.netlify.app")`

Then push the changes:
```bash
git add .
git commit -m "Update CORS for production"
git push
```

Railway will automatically redeploy.

---

## Step 4: Deploy Frontend to Netlify

### 4.1 Sign Up for Netlify
1. Go to https://www.netlify.com/
2. Click "Sign up" and use GitHub
3. Authorize Netlify

### 4.2 Update Frontend API URL
Before deploying, update the API base URL in your frontend code:

In `frontend/backend-integration.js`, change:
```javascript
const API_BASE_URL = 'https://YOUR_RAILWAY_BACKEND_URL/api';
```

Replace `YOUR_RAILWAY_BACKEND_URL` with your actual Railway backend URL.

### 4.3 Deploy Frontend
**Option A: Drag and Drop (Easiest)**
1. Go to Netlify dashboard
2. Click "Add new site" → "Deploy manually"
3. Drag and drop your entire `frontend` folder
4. Netlify will deploy and give you a URL

**Option B: GitHub Integration**
1. Push frontend to GitHub repository
2. In Netlify, click "Add new site" → "Import from Git"
3. Select your repository
4. Set build settings:
   - Build command: (leave empty)
   - Publish directory: `frontend`
5. Click "Deploy site"

### 4.4 Custom Domain (Optional)
1. In Netlify, go to "Domain settings"
2. Click "Add custom domain"
3. Follow instructions to configure your domain

---

## Step 5: Update CORS with Final Frontend URL

After Netlify deployment:

1. Copy your Netlify URL (e.g., `https://quickkart.netlify.app`)
2. Update `CorsConfig.java` with this exact URL
3. Push changes to GitHub
4. Railway will auto-redeploy

---

## Step 6: Test Your Live Application

### 6.1 Test Registration
1. Open your Netlify URL
2. Click "Register"
3. Create a new account as Customer
4. Verify password encryption is working

### 6.2 Test Customer Flow
1. Login as Customer
2. Browse products
3. Add items to cart
4. Place an order
5. Check order history

### 6.3 Test Vendor Flow
1. Register as Vendor
2. Add products
3. Check vendor orders
4. Verify stock management

---

## Quick Commands Reference

### Backend Deployment (Railway)
```bash
cd quickkart-backend/backend
git add .
git commit -m "Update message"
git push
```

### Frontend Deployment (Netlify)
Just drag and drop the `frontend` folder to Netlify dashboard, or:
```bash
cd quickkart-backend/frontend
# Update backend-integration.js with Railway URL
# Then drag-drop to Netlify
```

---

## Environment Variables Summary

### Railway Backend Environment Variables
```
SPRING_DATASOURCE_URL=mysql://${MYSQLHOST}:${MYSQLPORT}/${MYSQLDATABASE}
SPRING_DATASOURCE_USERNAME=${MYSQLUSER}
SPRING_DATASOURCE_PASSWORD=${MYSQLPASSWORD}
SPRING_JPA_HIBERNATE_DDL_AUTO=update
FRONTEND_URL=https://YOUR_NETLIFY_URL
```

---

## Troubleshooting

### Backend won't start
- Check Railway logs for errors
- Verify MySQL connection credentials
- Ensure Java version is set to 21 in `system.properties`

### CORS errors
- Verify CorsConfig.java has correct frontend URL
- Check that URL doesn't have trailing slash
- Redeploy backend after CORS changes

### Database connection issues
- Verify MySQL service is running on Railway
- Check environment variables are set correctly
- Ensure schema.sql has been executed

### Frontend API calls failing
- Verify API_BASE_URL in backend-integration.js
- Check Railway backend is running
- Open browser console for specific error messages

---

## Cost & Limits

### Railway (Free Tier)
- $5 free credit monthly
- 500 hours execution time
- 1GB RAM, 1GB disk
- Should be enough for small-medium traffic

### Netlify (Free Tier)
- 100GB bandwidth/month
- 300 build minutes/month
- Unlimited sites
- Perfect for static frontend

---

## Production Checklist

- [ ] Backend deployed to Railway
- [ ] MySQL database created and schema loaded
- [ ] Environment variables configured
- [ ] CORS settings updated with production URL
- [ ] Frontend deployed to Netlify
- [ ] API_BASE_URL updated in frontend
- [ ] Registration/Login tested
- [ ] Cart functionality tested
- [ ] Order placement tested
- [ ] Vendor features tested
- [ ] Password encryption verified
- [ ] All images loading correctly

---

## Next Steps After Going Live

1. **Monitor Railway Usage**: Check your credit usage in Railway dashboard
2. **Set Up Monitoring**: Consider adding application monitoring
3. **Backup Database**: Export MySQL data regularly
4. **Custom Domain**: Point your domain to Netlify
5. **SSL Certificate**: Netlify provides free SSL automatically
6. **Analytics**: Add Google Analytics to track users

---

## Support & Resources

- Railway Docs: https://docs.railway.app/
- Netlify Docs: https://docs.netlify.com/
- Spring Boot Deployment: https://spring.io/guides/gs/spring-boot/

---

**Your QuickKart application is now live! 🎉**

Share your Netlify URL with users and start testing!
