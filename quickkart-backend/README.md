# QuickKart - Hyperlocal E-commerce Platform

A full-stack hyperlocal e-commerce application built with Spring Boot and vanilla JavaScript.

## 🚀 Features

- **Customer Portal**: Browse products, manage cart, place orders
- **Vendor Portal**: Add products, manage inventory, view orders
- **User Authentication**: Secure login/registration with BCrypt password encryption
- **Stock Management**: Real-time stock tracking and updates
- **Order Management**: Complete order flow with notifications
- **Address Management**: Multiple delivery addresses
- **Pincode-based Delivery**: Hyperlocal product filtering

## 🛠️ Tech Stack

### Backend
- **Framework**: Spring Boot 3.3.5
- **Language**: Java 21
- **Database**: MySQL 8.0
- **Security**: BCrypt Password Encryption
- **ORM**: Hibernate/JPA
- **Build Tool**: Maven

### Frontend
- **Languages**: HTML5, CSS3, JavaScript (Vanilla)
- **Styling**: Custom CSS with responsive design
- **Images**: Cloudinary CDN with optimization

## 📦 Project Structure

```
quickkart-backend/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/quickkart/
│   │   │   │   ├── controller/     # REST API endpoints
│   │   │   │   ├── entity/         # JPA entities
│   │   │   │   ├── repository/     # Data access layer
│   │   │   │   └── config/         # Configuration classes
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   ├── pom.xml
│   ├── Procfile                    # Railway deployment
│   └── system.properties           # Java version
├── frontend/
│   ├── index.html                  # Landing page
│   ├── home.html                   # About page
│   ├── customer.html               # Customer portal
│   ├── vendor.html                 # Vendor portal
│   ├── login.html                  # Authentication
│   ├── backend-integration.js      # API integration
│   ├── style.css                   # Global styles
│   └── netlify.toml               # Netlify config
└── schema.sql                      # Database schema

```

## 🏃‍♂️ Local Development

### Prerequisites
- Java 21 or higher
- Maven 3.6+
- MySQL 8.0
- Git

### Backend Setup

1. **Clone the repository**
```bash
cd quickkart-backend/backend
```

2. **Configure Database**
Update `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/quickkart_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

3. **Create Database**
```sql
CREATE DATABASE quickkart_db;
```

4. **Run schema.sql** in your MySQL client

5. **Build and Run**
```bash
mvn clean install
mvn spring-boot:run
```

Backend will start at `http://localhost:8080`

### Frontend Setup

1. **Update API URL**
In `frontend/backend-integration.js`:
```javascript
const API_BASE_URL = 'http://localhost:8080/api';
```

2. **Open in Browser**
- Use Live Server extension in VS Code, or
- Open `index.html` directly in browser

## 🌐 Deployment

See [DEPLOYMENT-STEPS.md](DEPLOYMENT-STEPS.md) for detailed deployment instructions.

### Quick Deploy Summary

**Backend (Railway)**
1. Push code to GitHub
2. Create Railway project
3. Add MySQL database
4. Configure environment variables
5. Deploy from GitHub

**Frontend (Netlify)**
1. Update `API_BASE_URL` in `backend-integration.js`
2. Drag-drop `frontend` folder to Netlify
3. Get your live URL

## 📚 API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user

### Products
- `GET /api/products` - Get all products
- `GET /api/products/{id}` - Get product by ID
- `GET /api/products/pincode/{pincode}` - Get products by pincode
- `POST /api/products` - Add new product (Vendor)
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product

### Cart
- `GET /api/cart/{userId}` - Get user's cart
- `POST /api/cart` - Add item to cart
- `PUT /api/cart/{id}` - Update cart item
- `DELETE /api/cart/{id}` - Remove from cart

### Orders
- `GET /api/orders/user/{userId}` - Get user orders
- `GET /api/orders/vendor/{vendorId}` - Get vendor orders
- `POST /api/orders` - Place order
- `PUT /api/orders/{id}/status` - Update order status

### Addresses
- `GET /api/addresses/user/{userId}` - Get user addresses
- `POST /api/addresses` - Add address
- `PUT /api/addresses/{id}` - Update address
- `DELETE /api/addresses/{id}` - Delete address

## 🔐 Security Features

- BCrypt password hashing
- CORS configuration
- Input validation
- SQL injection protection via JPA

## 📊 Database Schema

Key entities:
- **Users**: Customer and Vendor accounts
- **Products**: Product catalog with vendor association
- **Cart**: Shopping cart items
- **Orders**: Order information
- **OrderItems**: Items in each order
- **Addresses**: Delivery addresses
- **Notifications**: User notifications

## 🐛 Known Issues & Limitations

- Image uploads are via URL (no file upload yet)
- No payment gateway integration
- No email notifications
- Limited to single currency

## 🔄 Future Enhancements

- [ ] Payment gateway integration (Razorpay/Stripe)
- [ ] Email notifications
- [ ] SMS notifications for order updates
- [ ] Image upload functionality
- [ ] Advanced search and filters
- [ ] Product reviews and ratings
- [ ] Wishlist feature
- [ ] Vendor analytics dashboard
- [ ] Multi-language support

## 👥 User Roles

### Customer
- Browse products by location
- Add to cart
- Place orders
- Track order history
- Manage delivery addresses

### Vendor
- Add/Edit/Delete products
- Manage inventory
- View incoming orders
- Update order status

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is open source and available under the MIT License.

## 📧 Contact

For questions or support, please open an issue in the repository.

---

**Built with ❤️ for local businesses and communities**
