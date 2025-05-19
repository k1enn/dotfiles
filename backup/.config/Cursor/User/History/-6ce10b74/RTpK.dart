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
                  _buildHeader(cryptoProvider),
                  const SizedBox(height: 24),
                  FlatContainer(
                    child: Column(
                      children: [
                        _buildPriceCard(selectedCrypto),
                        const SizedBox(height: 24),
                        _buildChart(selectedCrypto),
                        const SizedBox(height: 24),
                        _buildTransactionHistory(cryptoProvider),
                      ],
                    ),
                  ),
                ],
              ),
            );
          },
        ),
      ),
      bottomNavigationBar: _buildBottomNavigationBar(),
    );
  }

  Widget _buildHeader(CryptoProvider cryptoProvider) {
    return Padding(
      padding: const EdgeInsets.only(left: 4.0, top: 8.0, right: 4.0, bottom: 0),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
         Text(
            'App Loz',
            style: AppTextStyles.heading1,
          ),
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(15),
            ),
            child: const Icon(
              Icons.settings_rounded,
              color: AppColors.gunmetal,
              size: 24,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildPriceCard(CryptoCurrency crypto) {
    final priceChangeColor = crypto.priceChangePercentage24h >= 0
        ? AppColors.success
        : AppColors.error;

    return GestureDetector(
      onTap: () {
        Navigator.push(
          context,
          MaterialPageRoute(
            builder: (context) => CryptoDetailScreen(crypto: crypto),
          ),
        );
      },
      child: Container(
        padding: const EdgeInsets.all(12),
         decoration: BoxDecoration(
          color: AppColors.darkBlue,
          borderRadius: BorderRadius.circular(12),
          ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Text(
                  crypto.name,
                  style: AppTextStyles.heading2.copyWith(
                    color: AppColors.white,
                  ),
                ),
                const SizedBox(width: 8),
                Container(
                  padding: const EdgeInsets.symmetric(
                    vertical: 9,
                    horizontal: 12,
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
            const SizedBox(height: 16),
            Text(
              '\$${crypto.price.toStringAsFixed(2)}',
              style: AppTextStyles.price.copyWith(
              color: AppColors.white,),
            ),
            const SizedBox(height: 8),
            Text(
              'Price change (24h): \$${crypto.priceChange24h.toStringAsFixed(2)}',
              style: AppTextStyles.caption.copyWith(
                color: AppColors.white,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Container _buildChart(CryptoCurrency crypto) {
    return Container(
      padding: const EdgeInsets.only(top: 2, bottom: 8, left: 2, right: 2),
      decoration: BoxDecoration(
        color: AppColors.background,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
            color: AppColors.white,
            offset: Offset(0, 2),
            blurRadius: 4,
            spreadRadius: 0,
          ),
          BoxShadow(
            color: AppColors.white,
            offset: Offset(0, -2),
            blurRadius: 4,
            spreadRadius: 0,
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Price Chart (24h)',
            style: AppTextStyles.heading3,
          ),
          const SizedBox(height: 50),
          CryptoCandleStickChart(crypto: crypto),
        ],
      ),
    );
  }

  Widget _buildTransactionHistory(CryptoProvider cryptoProvider) {
    final transactions = cryptoProvider.transactions;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'Recent Transactions',
          style: AppTextStyles.heading3,
        ),
        const SizedBox(height: 16),
        if (transactions.isEmpty)
          const Center(
            child: Text('No transactions yet.'),
          )
        else
          ListView.builder(
            shrinkWrap: true,
            physics: const NeverScrollableScrollPhysics(),
            itemCount: transactions.length > 5 ? 5 : transactions.length,
            itemBuilder: (context, index) {
              return TransactionItem(
                transaction: transactions[index],
              );
            },
          ),
      ],
    );
  }

  Widget _buildBottomNavigationBar() {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        boxShadow: [
          BoxShadow(
            color: Colors.grey.withOpacity(0.2),
            blurRadius: 5,
            offset: const Offset(0, -1),
          ),
        ],
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 8),
        child: BottomNavigationBar(
          currentIndex: _selectedIndex,
          onTap: (index) {
            setState(() {
              _selectedIndex = index;
            });
          },
          backgroundColor: Colors.transparent,
          selectedItemColor: AppColors.accent,
          unselectedItemColor: AppColors.gunmetal.withOpacity(0.5),
          type: BottomNavigationBarType.fixed,
          elevation: 0,
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