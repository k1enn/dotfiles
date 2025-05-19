import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'colors.dart';

class AppTextStyles {
  static TextStyle get heading1 => const TextStyle(
        fontSize: 28,
        fontWeight: FontWeight.bold,
        color: AppColors.textPrimary,
        fontFamily: 'Inter',
      );
  
  static TextStyle get heading2 => const TextStyle(
        fontSize: 24,
        fontWeight: FontWeight.bold,
        color: AppColors.textPrimary,
        fontFamily: 'Inter',
      );
  
  static TextStyle get heading3 => const TextStyle(
        fontSize: 20,
        fontWeight: FontWeight.w600,
        color: AppColors.textPrimary,
        fontFamily: 'Inter',
      );
  
  static TextStyle get body => const TextStyle(
        fontSize: 16,
        color: AppColors.textPrimary,
        fontFamily: 'Inter',
      );
  
  static TextStyle get bodyBold => const TextStyle(
        fontSize: 16,
        fontWeight: FontWeight.bold,
        color: AppColors.textPrimary,
        fontFamily: 'Inter',
      );
  
  static TextStyle get caption => TextStyle(
        fontSize: 14,
        // ignore: deprecated_member_use
        color: AppColors.textPrimary.withOpacity(0.7),
        fontFamily: 'Inter',
      );
  
  static TextStyle get button => const TextStyle(
        fontSize: 16,
        fontWeight: FontWeight.w600,
        color: AppColors.textPrimary,
        fontFamily: 'Inter',
      );
  
  static TextStyle get price => const TextStyle(
        fontSize: 32,
        fontWeight: FontWeight.bold,
        color: AppColors.textPrimary,
        fontFamily: 'Inter',
      );
} 