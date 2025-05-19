// ignore_for_file: deprecated_member_use

import 'package:flutter/material.dart';
import '../constants/colors.dart';
import '../constants/text_styles.dart';
import '../models/crypto_currency.dart';
import 'neumorphic_container.dart';

class TransactionItem extends StatelessWidget {
  final Transaction transaction;

  const TransactionItem({
    super.key,
    required this.transaction,
  });

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 16.0),
        child: Container(
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: AppColors.accent,
            borderRadius: BorderRadius.circular(12),
          ),
          child: Row(
          children: [
            // Transaction type icon (up or down arrow)
            _buildTransactionIcon(),
            const SizedBox(width: 16),
            // Transaction details
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  
                  Text(
                    transaction.type == TransactionType.buy
                        ? 'Bought ${transaction.cryptoSymbol}'
                        : 'Sold ${transaction.cryptoSymbol}',
                    style: AppTextStyles.bodyBold,
                  ),
                  const SizedBox(height: 4),
                  Text(
                    '${transaction.date.day}/${transaction.date.month}/${transaction.date.year} at ${transaction.date.hour}:${transaction.date.minute.toString().padLeft(2, '0')}',
                    style: AppTextStyles.caption,
                  ),
                ],
              ),
            ),
            // Amount and value
            Column(
              crossAxisAlignment: CrossAxisAlignment.end,
              children: [
                Text(
                  '${transaction.type == TransactionType.buy ? '+' : '-'}${transaction.amount} ${transaction.cryptoSymbol}',
                  style: TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                    color: transaction.type == TransactionType.buy
                        ? AppColors.success
                        : AppColors.error,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  '\$${transaction.total.toStringAsFixed(2)}',
                  style: AppTextStyles.caption,
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildTransactionIcon() {
    return Container(
      width: 40,
      height: 40,
      decoration: BoxDecoration(
        color: transaction.type == TransactionType.buy
            ? AppColors.success.withOpacity(0.2)
            : AppColors.error.withOpacity(0.2),
        borderRadius: BorderRadius.circular(20),
      ),
      child: Icon(
        transaction.type == TransactionType.buy
            ? Icons.arrow_upward
            : Icons.arrow_downward,
        color: transaction.type == TransactionType.buy
            ? AppColors.success
            : AppColors.error,
        size: 20,
      ),
    );
  }
} 