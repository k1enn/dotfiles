import 'package:flutter/material.dart';
import '../models/crypto_currency.dart';

class CryptoProvider extends ChangeNotifier {
  List<CryptoCurrency> _cryptocurrencies = [];
  List<Transaction> _transactions = [];
  String _selectedCryptoId = 'bitcoin';
  bool _isLoading = false;

  CryptoProvider() {
    // Initialize with sample data for development
    _cryptocurrencies = CryptoCurrency.getSampleData();
    _transactions = Transaction.getSampleData();
  }

  // Getters
  List<CryptoCurrency> get cryptocurrencies => _cryptocurrencies;
  List<Transaction> get transactions => _transactions;
  String get selectedCryptoId => _selectedCryptoId;
  bool get isLoading => _isLoading;

  CryptoCurrency get selectedCrypto {
    return _cryptocurrencies.firstWhere(
      (crypto) => crypto.id == _selectedCryptoId,
      orElse: () => _cryptocurrencies.first,
    );
  }

  // Methods
  void selectCrypto(String id) {
    _selectedCryptoId = id;
    notifyListeners();
  }

  // This method would fetch real data from an API in production
  Future<void> fetchCryptocurrencies() async {
    _isLoading = true;
    notifyListeners();

    // Simulate API delay
    await Future.delayed(const Duration(seconds: 2));
    
    // For development, we'll use sample data
    _cryptocurrencies = CryptoCurrency.getSampleData();
    _isLoading = false;
    notifyListeners();
  }

  // This method would fetch real transaction data in production
  Future<void> fetchTransactions() async {
    _isLoading = true;
    notifyListeners();

    // Simulate API delay
    await Future.delayed(const Duration(seconds: 1));
    
    // For development, we'll use sample data
    _transactions = Transaction.getSampleData();
    _isLoading = false;
    notifyListeners();
  }

  // Add a new transaction (for buy/sell functionality)
  void addTransaction(Transaction transaction) {
    _transactions.add(transaction);
    notifyListeners();
  }
} 