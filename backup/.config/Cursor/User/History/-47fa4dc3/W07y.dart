import 'package:flutter/material.dart';
import 'package:fl_chart/fl_chart.dart';
import '../constants/colors.dart';
import '../models/crypto_currency.dart';
import '../constants/text_styles.dart';

class CryptoCandleStickChart extends StatelessWidget {
  final CryptoCurrency crypto;
  final double height;

  const CryptoCandleStickChart({
    super.key,
    required this.crypto,
    this.height = 250,
  });

  @override
  Widget build(BuildContext context) {
    // For demonstration purposes, let's generate some sample candlestick data
    final List<CandleData> candleData = _generateSampleCandleData();

    return SizedBox(
      height: height,
      child: LineChart(
        LineChartData(
          gridData: FlGridData(show: false),
          titlesData: FlTitlesData(
            show: true,
            topTitles: AxisTitles(sideTitles: SideTitles(showTitles: false)),
            rightTitles: AxisTitles(sideTitles: SideTitles(showTitles: false)),
            bottomTitles: AxisTitles(
              sideTitles: SideTitles(
                showTitles: true,
                getTitlesWidget: (value, meta) {
                  // Show a few date labels
                  if (value % 6 == 0 && value < historicalData.length) {
                    final date = historicalData[value.toInt()].time;
                    return Padding(
                      padding: const EdgeInsets.only(top: 8.0),
                      child: Text(
                        '${date.day}/${date.month}',
                        style: AppTextStyles.caption,
                      ),
                    );
                  }
                  return const SizedBox();
                },
              ),
            ),
            leftTitles: AxisTitles(
              sideTitles: SideTitles(
                showTitles: true,
                reservedSize: 60,
                getTitlesWidget: (value, meta) {
                  return Text(
                    '\$${value.toInt()}',
                    style: AppTextStyles.caption,
                  );
                },
              ),
            ),
          ),
          borderData: FlBorderData(show: false),
          lineBarsData: [
            LineChartBarData(
              spots: historicalData.asMap().entries.map((entry) {
                return FlSpot(entry.key.toDouble(), entry.value.price);
              }).toList(),
              isCurved: true,
              color: lineColor,
              barWidth: 3,
              isStrokeCapRound: true,
              dotData: FlDotData(show: false),
              belowBarData: BarAreaData(
                show: true,
                // ignore: deprecated_member_use
                color: lineColor.withOpacity(0.2),
              ),
            ),
          ],
        ),
      ),
    );
  }

  double _getMaxPrice(List<CandleData> data) {
    return data.map((e) => e.high).reduce((a, b) => a > b ? a : b);
  }

  double _getMinPrice(List<CandleData> data) {
    return data.map((e) => e.low).reduce((a, b) => a < b ? a : b);
  }

  List<CandleData> _generateSampleCandleData() {
    // For demonstration, generate sample candlestick data based on crypto price
    List<CandleData> result = [];
    double basePrice = crypto.price;
    
    for (int i = 0; i < 30; i++) {
      double volatility = (basePrice * 0.02) * 
          ((DateTime.now().millisecond + i) % 100) / 100;
      
      double open = basePrice + (volatility * 0.5) - (volatility * 0.25);
      double close = open + (volatility * 0.7) - (volatility * 0.35);
      double high = Math.max(open, close) + (volatility * 0.2);
      double low = Math.min(open, close) - (volatility * 0.2);
      
      result.add(CandleData(open, high, low, close));
      
      // Shift base price for next candle
      basePrice = close;
    }
    
    return result;
  }
}

class CryptoLineChart extends StatelessWidget {
  final CryptoCurrency crypto;
  final Color lineColor;
  final double height;

  const CryptoLineChart({
    super.key,
    required this.crypto,
    this.lineColor = AppColors.accent,
    this.height = 200,
  });

  @override
  Widget build(BuildContext context) {
    // Use the historical data from crypto, or generate if not available
    final List<PricePoint> historicalData = 
        crypto.historicalData ?? PricePoint.getRandomData();

    return SizedBox(
      height: height,
      child: LineChart(
        LineChartData(
          gridData: FlGridData(show: false),
          titlesData: FlTitlesData(
            show: true,
            topTitles: AxisTitles(sideTitles: SideTitles(showTitles: false)),
            rightTitles: AxisTitles(sideTitles: SideTitles(showTitles: false)),
            bottomTitles: AxisTitles(
              sideTitles: SideTitles(
                showTitles: true,
                getTitlesWidget: (value, meta) {
                  // Show a few date labels
                  if (value % 6 == 0 && value < historicalData.length) {
                    final date = historicalData[value.toInt()].time;
                    return Padding(
                      padding: const EdgeInsets.only(top: 8.0),
                      child: Text(
                        '${date.day}/${date.month}',
                        style: AppTextStyles.caption,
                      ),
                    );
                  }
                  return const SizedBox();
                },
              ),
            ),
            leftTitles: AxisTitles(
              sideTitles: SideTitles(
                showTitles: true,
                reservedSize: 60,
                getTitlesWidget: (value, meta) {
                  return Text(
                    '\$${value.toInt()}',
                    style: AppTextStyles.caption,
                  );
                },
              ),
            ),
          ),
          borderData: FlBorderData(show: false),
          lineBarsData: [
            LineChartBarData(
              spots: historicalData.asMap().entries.map((entry) {
                return FlSpot(entry.key.toDouble(), entry.value.price);
              }).toList(),
              isCurved: true,
              color: lineColor,
              barWidth: 3,
              isStrokeCapRound: true,
              dotData: FlDotData(show: false),
              belowBarData: BarAreaData(
                show: true,
                // ignore: deprecated_member_use
                color: lineColor.withOpacity(0.2),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

// Helper class for CandleStick chart
class CandleData {
  final double open;
  final double high;
  final double low;
  final double close;

  CandleData(this.open, this.high, this.low, this.close);
}

// Utility class for Math operations
class Math {
  static double max(double a, double b) => a > b ? a : b;
  static double min(double a, double b) => a < b ? a : b;
} 