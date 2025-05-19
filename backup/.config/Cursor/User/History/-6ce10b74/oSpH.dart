// ignore_for_file: library_private_types_in_public_api, deprecated_member_use

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../constants/colors.dart';
import '../constants/text_styles.dart';
import '../models/crypto_currency.dart';
import '../providers/crypto_provider.dart';
import '../widgets/crypto_charts.dart';
import '../widgets/flat_container.dart';
import '../widgets/transaction_item.dart';
import 'crypto_detail_screen.dart';

class DashboardScreen extends StatefulWidget {
  const DashboardScreen({super.key});

  @override
  _DashboardScreenState createState() => _DashboardScreenState();
}

class _DashboardScreenState extends State<DashboardScreen> {
  int _selectedIndex = 0;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.background,
      body: SafeArea(
        child: Consumer<CryptoProvider>(
          builder: (context, cryptoProvider, _) {
            final selectedCrypto = cryptoProvider.selectedCrypto;
            return SingleChildScrollView(
              padding: const EdgeInsets.all(2.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _buildHeader(),
                  const SizedBox(height: 24),
                  _buildCryptoHorizontalList(cryptoProvider),
                  const SizedBox(height: 24),
                ],
              ),
            );
          },
        ),
      ),
      bottomNavigationBar: _buildBottomNavigationBar(),
    );
  }

  Widget _buildHeader() {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 24.0, vertical: 12.0),
      child: Row(
        children: [
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'Welcome Back',
                style: AppTextStyles.caption.copyWith(
                  color: AppColors.textSecondary,
                ),
              ),
              const SizedBox(height: 4),
              Text(
                'My Wallet',
                style: AppTextStyles.heading1.copyWith(
                  color: AppColors.textPrimary,
                ),
              ),
            ],
          ),
          const Spacer(),
          Container(
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              color: AppColors.background,
              borderRadius: BorderRadius.circular(12),
              boxShadow: [
                BoxShadow(
                  color: AppColors.textPrimary.withOpacity(0.5),
                  blurRadius: 8,
                  spreadRadius: 1,
                ),
              ],
            ),
            child: Icon(
              Icons.notifications_none,
              color: AppColors.gunmetal,
              size: 24,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildCryptoHorizontalList(CryptoProvider cryptoProvider) {
    final cryptoList = cryptoProvider.cryptocurrencies;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 24.0),
          child: Text('Portfolio', style: AppTextStyles.heading3),
        ),
        const SizedBox(height: 16),
        SizedBox(
          height: 180,
          child: ListView.builder(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            scrollDirection: Axis.horizontal,
            itemCount: cryptoList.length,
            itemBuilder: (context, index) {
              return _buildCryptoCard(cryptoList[index], cryptoProvider);
            },
          ),
        ),
      ],
    );
  }

  Widget _buildCryptoCard(CryptoCurrency crypto, CryptoProvider provider) {
    final priceChangeColor =
        crypto.priceChangePercentage24h >= 0
            ? AppColors.success
            : AppColors.error;
    final isSelected = provider.selectedCryptoId == crypto.id;

    return GestureDetector(
      onTap: () {
        provider.selectCrypto(crypto.id);
        Navigator.push(
          context,
          MaterialPageRoute(
            builder: (context) => CryptoDetailScreen(crypto: crypto),
          ),
        );
      },
      child: Container(
        width: 200,
        margin: const EdgeInsets.only(right: 8, left: 0, top: 2, bottom: 8),
        padding: const EdgeInsets.all(16.0),
        decoration: BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: _getCryptoCardColors(crypto.id, isSelected),
          ),
          borderRadius: BorderRadius.circular(24),
          boxShadow: [
            BoxShadow(
              color: AppColors.accent.withOpacity(0.3),
              blurRadius: 4,
              offset: Offset(0, 4),
            ),
          ],
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  crypto.symbol,
                  style: AppTextStyles.heading2.copyWith(
                    color: AppColors.white,
                  ),
                ),
                Container(
                  padding: const EdgeInsets.symmetric(
                    vertical: 6,
                    horizontal: 8,
                  ),
                  decoration: BoxDecoration(
                    color: AppColors.navyBlue.withOpacity(0.2),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Text(
                    '${crypto.priceChangePercentage24h >= 0 ? '+' : ''}${crypto.priceChangePercentage24h.toStringAsFixed(2)}%',
                    style: TextStyle(
                      fontSize: 14,
                      fontWeight: FontWeight.bold,
                      color: priceChangeColor,
                    ),
                  ),
                ),
              ],
            ),
            const Spacer(),
            Text(
              crypto.name,
              style: AppTextStyles.caption.copyWith(
                color: AppColors.white.withOpacity(0.8),
              ),
            ),
            const SizedBox(height: 8),
            Text(
              '\$${crypto.price.toStringAsFixed(2)}',
              style: AppTextStyles.price.copyWith(
                color: AppColors.white,
                fontSize: 24,
              ),
            ),
            const SizedBox(height: 4),
            Text(
              '24h: \$${crypto.priceChange24h.toStringAsFixed(2)}',
              style: AppTextStyles.caption.copyWith(
                color: AppColors.white.withOpacity(0.8),
              ),
            ),
          ],
        ),
      ),
    );
  }

  List<Color> _getCryptoCardColors(String cryptoId, bool isSelected) {
    switch (cryptoId) {
      case 'bitcoin':
        return [
          isSelected ? Colors.orange[800]! : Colors.orange[700]!,
          isSelected ? Colors.orange[400]! : Colors.orange[300]!,
        ];
      case 'litecoin':
        return [
          isSelected ? Colors.blue[800]! : Colors.blue[700]!,
          isSelected ? Colors.blue[400]! : Colors.blue[300]!,
        ];
      case 'ripple':
        return [
          isSelected ? Colors.purple[800]! : Colors.purple[700]!,
          isSelected ? Colors.purple[400]! : Colors.purple[300]!,
        ];
      case 'ethereum':
        return [
          isSelected ? Colors.green[800]! : Colors.green[700]!,
          isSelected ? Colors.green[400]! : Colors.green[300]!,
        ];
      default:
        return [AppColors.darkBlue, AppColors.accent];
    }
  }

  Widget _buildBottomNavigationBar() {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: const BorderRadius.only(
          topLeft: Radius.circular(24),
          topRight: Radius.circular(24),
        ),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.04),
            blurRadius: 8,
            spreadRadius: 1,
          ),
        ],
      ),
      child: ClipRRect(
        borderRadius: const BorderRadius.only(
          topLeft: Radius.circular(24),
          topRight: Radius.circular(24),
        ),
        child: BottomNavigationBar(
          currentIndex: _selectedIndex,
          onTap: (index) => setState(() => _selectedIndex = index),
          backgroundColor: Colors.white,
          selectedItemColor: AppColors.accent,
          unselectedItemColor: AppColors.gunmetal.withOpacity(0.4),
          type: BottomNavigationBarType.fixed,
          elevation: 0,
          selectedLabelStyle: TextStyle(
            fontWeight: FontWeight.w600,
            fontSize: 12,
          ),
          unselectedLabelStyle: TextStyle(fontSize: 11),
          items: const [
            BottomNavigationBarItem(
              icon: Icon(Icons.bar_chart),
              label: 'Charts',
            ),
            BottomNavigationBarItem(
              icon: Icon(Icons.account_balance_wallet),
              label: 'Wallet',
            ),
            BottomNavigationBarItem(
              icon: Icon(Icons.notifications),
              label: 'Alerts',
            ),
          ],
        ),
      ),
    );
  }
}
