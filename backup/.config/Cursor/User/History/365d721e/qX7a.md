# Cryptocurrency Wallet App

A beautiful cryptocurrency wallet application built with Flutter featuring a neumorphic design style. This app provides an elegant interface for tracking and managing various cryptocurrencies.

## Features

- **Neumorphic Design**: Soft shadows and clean interface with the flutter_neumorphic package
- **Cryptocurrency Tracking**: Monitor Bitcoin, Litecoin, and Ripple prices
- **Interactive Charts**: View candlestick and line charts showing price movements
- **Transaction History**: Track buy and sell transactions with up/down indicators
- **Responsive Layout**: Beautiful design that works across different screen sizes

## Screenshots

*Screenshots will be added after the first build*

## Getting Started

### Prerequisites

- Flutter SDK v3.7.2 or higher
- Dart SDK v3.0.0 or higher

### Installation

1. Clone the repository
```bash
git clone https://github.com/yourusername/crypto_currency_app.git
```

2. Navigate to the project directory
```bash
cd crypto_currency_app
```

3. Install dependencies
```bash
flutter pub get
```

4. Run the app
```bash
flutter run
```

## Architecture

The app follows a clean architecture approach with:

- **Models**: Data models like CryptoCurrency and Transaction
- **Providers**: State management using Provider package
- **Screens**: UI screens for different app sections
- **Widgets**: Reusable UI components
- **Constants**: App-wide constants for colors and text styles

## Design System

The application uses a custom color scheme:
- Timberwolf: #D0D2D1
- Amaranth Purple: #A32F40
- Earth Yellow: #E9B969
- Gunmetal: #282C37
- White: #FEFEFE

## Dependencies

- flutter_neumorphic: ^3.2.0
- fl_chart: ^0.63.0
- google_fonts: ^4.0.4
- provider: ^6.0.5
- http: ^1.1.0
- shared_preferences: ^2.2.2

## Future Enhancements

- Add authentication system
- Implement real-time cryptocurrency data via APIs
- Add buy/sell cryptocurrency functionality
- Support for more cryptocurrencies
- Dark mode support
- Portfolio analytics

## License

This project is licensed under the MIT License - see the LICENSE file for details.
