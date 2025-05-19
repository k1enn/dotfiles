## Basic things
## Branch rule
**Structure Your Branch Names**

Follow this general structure for branch names:
```
<type>/<optional-group>/<description>
```

**Examples**:
```
feature/user-authentication

feature/payment/stripe-integration

bugfix/login-error
```
## **Backend**
### **Backend Files Structure**
This is just an prototype, would change later based on needs.
```
backend/
├── app.js (or index.js)    # Application entry point
├── config/                 # Configuration files
│   ├── db.js               # Database connection
│   └── env.js              # Environment variables
├── controllers/            # Request handlers
│   ├── authController.js
│   ├── productController.js
│   └── orderController.js
├── models/                 # Database models
│   ├── User.js
│   ├── Product.js
│   └── Order.js
├── routes/                 # API routes
│   ├── api.js              # Main router
│   ├── auth.js
│   └── products.js
├── middleware/             # Custom middleware
│   ├── auth.js             # Authentication middleware
│   ├── errorHandler.js
│   └── logger.js
├── utils/                  # Helper functions
│   ├── validation.js
│   └── helpers.js
└── tests/                  # Test files
    ├── unit/
    └── integration/

```
### **To do**
- Connect to MongoDB
- Create data models for products, users, orders, and other entities
- Build RESTful API endpoints for your e-commerce functionality including:
    - User authentication and authorization
    - Product management
    - Order processing
    - Payment integration
    - Others...
## Frontend
### Frontend Files Structure
Can change anything when needed.
```
frontend/src/
├── components/    # Reusable UI components
├── pages/         # Main application pages
├── services/      # API service calls
├── context/       # State management
└── utils/         # Utility functions
```
### **To do**
- Implement essential components:
    - Header/Navigation component
    - Product listing and filters
    - Product detail pages
    - Shopping cart
    - Checkout process
    - User authentication forms
- Connect your frontend to the backend API using Fetch or Axios