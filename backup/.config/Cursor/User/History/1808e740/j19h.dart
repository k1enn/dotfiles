class CryptoCurrency {
  final String id;
  final String name;
  final String symbol;
  final double price;
  final double priceChange24h;
  final double priceChangePercentage24h;
  final double high24h;
  final double low24h;
  final List<PricePoint>? historicalData;

  CryptoCurrency({
    required this.id,
    required this.name,
    required this.symbol,
    required this.price,
    required this.priceChange24h,
    required this.priceChangePercentage24h,
    required this.high24h,
    required this.low24h,
    this.historicalData,
  });

  factory CryptoCurrency.fromJson(Map<String, dynamic> json) {
    return CryptoCurrency(
      id: json['id'],
      name: json['name'],
      symbol: json['symbol'],
      price: json['current_price'].toDouble(),
      priceChange24h: json['price_change_24h'].toDouble(),
      priceChangePercentage24h: json['price_change_percentage_24h'].toDouble(),
      high24h: json['high_24h'].toDouble(),
      low24h: json['low_24h'].toDouble(),
      historicalData: null,
    );
  }

  // Sample data for development and testing
  static List<CryptoCurrency> getSampleData() {
    return [
      CryptoCurrency(
        id: 'bitcoin',
        name: 'Bitcoin',
        symbol: 'BTC',
        price: 36789.23,
        priceChange24h: 1243.12,
        priceChangePercentage24h: 3.49,
        high24h: 37012.45,
        low24h: 35621.89,
        historicalData: PricePoint.getRandomData(),
      ),
      CryptoCurrency(
        id: 'litecoin',
        name: 'Litecoin',
        symbol: 'LTC',
        price: 142.78,
        priceChange24h: -5.23,
        priceChangePercentage24h: -3.54,
        high24h: 148.92,
        low24h: 140.56,
        historicalData: PricePoint.getRandomData(),
      ),
      CryptoCurrency(
        id: 'ripple',
        name: 'Ripple',
        symbol: 'XRP',
        price: 0.4352,
        priceChange24h: 0.0213,
        priceChangePercentage24h: 5.14,
        high24h: 0.4512,
        low24h: 0.4112,
        historicalData: PricePoint.getRandomData(),
      ),
    ];
  }
}

class PricePoint {
  final DateTime time;
  final double price;

  PricePoint({required this.time, required this.price});

  // Generate random historical data for development and testing
  static List<PricePoint> getRandomData() {
    final now = DateTime.now();
    final List<PricePoint> data = [];
    double basePrice = 35000 + (DateTime.now().millisecond % 5000);

    for (int i = 0; i < 30; i++) {
      final time = now.subtract(Duration(days: 29 - i));
      final randomChange = (DateTime.now().microsecond % 1000) / 100 - 5;
      basePrice += randomChange;
      data.add(PricePoint(time: time, price: basePrice));
    }

    return data;
  }
}

class Transaction {
  final String id;
  final String cryptoId;
  final String cryptoSymbol;
  final double amount;
  final double price;
  final DateTime date;
  final TransactionType type;

  Transaction({
    required this.id,
    required this.cryptoId,
    required this.cryptoSymbol,
    required this.amount,
    required this.price,
    required this.date,
    required this.type,
  });

  double get total => amount * price;

  // Sample data for development and testing
  static List<Transaction> getSampleData() {
    return [
      Transaction(
        id: '1',
        cryptoId: 'bitcoin',
        cryptoSymbol: 'BTC',
        amount: 0.05,
        price: 36750.42,
        date: DateTime.now().subtract(const Duration(days: 2)),
        type: TransactionType.buy,
      ),
      Transaction(
        id: '2',
        cryptoId: 'litecoin',
        cryptoSymbol: 'LTC',
        amount: 2.5,
        price: 143.21,
        date: DateTime.now().subtract(const Duration(days: 5)),
        type: TransactionType.buy,
      ),
      Transaction(
        id: '3',
        cryptoId: 'bitcoin',
        cryptoSymbol: 'BTC',
        amount: 0.02,
        price: 36120.88,
        date: DateTime.now().subtract(const Duration(days: 7)),
        type: TransactionType.sell,
      ),
      Transaction(
        id: '4',
        cryptoId: 'ripple',
        cryptoSymbol: 'XRP',
        amount: 100,
        price: 0.4312,
        date: DateTime.now().subtract(const Duration(days: 10)),
        type: TransactionType.buy,
      ),
    ];
  }
}

enum TransactionType { buy, sell } 