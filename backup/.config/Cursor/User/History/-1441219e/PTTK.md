## Basic things

### Backend Files Structure
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

### Frontend Files Structure
```
frontend/src/
├── components/    # Reusable UI components
├── pages/         # Main application pages
├── services/      # API service calls
├── context/       # State management
└── utils/         # Utility functions
```