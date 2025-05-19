import 'package:flutter/widgets.dart';
import '../constants/colors.dart';
import '../constants/text_styles.dart';
import '../models/crypto_currency.dart';
import '../widgets/crypto_charts.dart';
import '../widgets/neumorphic_container.dart';

class CryptoDetailScreen extends StatefulWidget {
  final CryptoCurrency crypto;

  const CryptoDetailScreen({
    super.key,
    required this.crypto,
  });

  @override
  // ignore: library_private_types_in_public_api
  _CryptoDetailScreenState createState() => _CryptoDetailScreenState();
}

class _CryptoDetailScreenState extends State<CryptoDetailScreen> {
  String _timeRange = '1W'; // Default time range is 1 week

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: _buildAppBar(),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _buildHeader(),
              const SizedBox(height: 24),
              _buildTimeRangeSelector(),
              const SizedBox(height: 24),
              _buildPriceChart(),
              const SizedBox(height: 24),
              _buildPriceStatistics(),
            ],
          ),
        ),
      ),
    );
  }

  PreferredSizeWidget _buildAppBar() {
    return AppBar(
      backgroundColor: Colors.transparent,
      elevation: 0,
      leading: NeumorphicButton(
        onPressed: () => Navigator.of(context).pop(),
        style: const NeumorphicStyle(
          depth: 4,
          intensity: 0.8,
          boxShape: NeumorphicBoxShape.circle(),
          color: AppColors.background,
        ),
        child: const Icon(
          Icons.arrow_back,
          color: AppColors.gunmetal,
        ),
      ),
      title: Text(
        widget.crypto.name,
        style: AppTextStyles.heading2,
      ),
      centerTitle: true,
    );
  }

  Widget _buildHeader() {
    final priceChangeColor = widget.crypto.priceChangePercentage24h >= 0
        ? AppColors.success
        : AppColors.error;

    return NeumorphicContainer(
      padding: const EdgeInsets.all(24),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text(
                '\$${widget.crypto.price.toStringAsFixed(2)}',
                style: AppTextStyles.price,
              ),
              const SizedBox(width: 12),
              Container(
                padding: const EdgeInsets.symmetric(
                  vertical: 6,
                  horizontal: 12,
                ),
                decoration: BoxDecoration(
                  // ignore: deprecated_member_use
                  color: priceChangeColor.withOpacity(0.2),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Text(
                  '${widget.crypto.priceChangePercentage24h >= 0 ? '+' : ''}${widget.crypto.priceChangePercentage24h.toStringAsFixed(2)}%',
                  style: TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.bold,
                    color: priceChangeColor,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: [
              _buildPriceInfoItem(
                'Low',
                '\$${widget.crypto.low24h.toStringAsFixed(2)}',
              ),
              _buildPriceInfoItem(
                'High',
                '\$${widget.crypto.high24h.toStringAsFixed(2)}',
              ),
              _buildPriceInfoItem(
                'Change',
                '\$${widget.crypto.priceChange24h.toStringAsFixed(2)}',
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildPriceInfoItem(String label, String value) {
    return Column(
      children: [
        Text(
          label,
          style: AppTextStyles.caption.copyWith(
            // ignore: deprecated_member_use
            color: AppColors.gunmetal.withOpacity(0.7),
          ),
        ),
        const SizedBox(height: 8),
        Text(
          value,
          style: AppTextStyles.bodyBold,
        ),
      ],
    );
  }

  Widget _buildTimeRangeSelector() {
    final timeRanges = ['1D', '1W', '1M', '3M', '1Y', 'All'];

    return NeumorphicContainer(
      padding: const EdgeInsets.all(8),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceAround,
        children: timeRanges.map((range) {
          final isSelected = range == _timeRange;
          return GestureDetector(
            onTap: () {
              setState(() {
                _timeRange = range;
              });
            },
            child: Container(
              padding: const EdgeInsets.symmetric(
                vertical: 8,
                horizontal: 16,
              ),
              decoration: BoxDecoration(
                color: isSelected
                    ? AppColors.accent
                    : Colors.transparent,
                borderRadius: BorderRadius.circular(20),
              ),
              child: Text(
                range,
                style: AppTextStyles.button.copyWith(
                  color: isSelected
                      ? Colors.white
                      // ignore: deprecated_member_use
                      : AppColors.gunmetal.withOpacity(0.7),
                  fontWeight: isSelected
                      ? FontWeight.bold
                      : FontWeight.normal,
                ),
              ),
            ),
          );
        }).toList(),
      ),
    );
  }

  Widget _buildPriceChart() {
    return NeumorphicContainer(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Price Chart ($_timeRange)',
            style: AppTextStyles.heading3,
          ),
          const SizedBox(height: 16),
          CryptoLineChart(
            crypto: widget.crypto,
            height: 250,
          ),
        ],
      ),
    );
  }

  Widget _buildPriceStatistics() {
    return NeumorphicContainer(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Statistics',
            style: AppTextStyles.heading3,
          ),
          const SizedBox(height: 16),
          _buildStatisticRow('Market Cap', '\$${(widget.crypto.price * 19000000).toStringAsFixed(0)}'),
          _buildStatisticRow('Volume (24h)', '\$${(widget.crypto.price * 500000).toStringAsFixed(0)}'),
          _buildStatisticRow('Circulating Supply', '${widget.crypto.symbol} ${(19000000).toString()}'),
          _buildStatisticRow('Total Supply', '${widget.crypto.symbol} ${(21000000).toString()}'),
          _buildStatisticRow('Rank', '#1'),
        ],
      ),
    );
  }

  Widget _buildStatisticRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12.0),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            label,
            style: AppTextStyles.body.copyWith(
              // ignore: deprecated_member_use
              color: AppColors.gunmetal.withOpacity(0.7),
            ),
          ),
          Text(
            value,
            style: AppTextStyles.bodyBold,
          ),
        ],
      ),
    );
  }
} 