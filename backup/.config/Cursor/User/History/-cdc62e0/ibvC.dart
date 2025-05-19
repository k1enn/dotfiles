import 'package:flutter/material.dart';
import '../constants/colors.dart';
import '../constants/text_styles.dart';
import '../widgets/flat_container.dart';
import 'dashboard_screen.dart';

class SplashScreen extends StatelessWidget {
  const SplashScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(24.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Spacer(flex: 1),
              // Diamond logo
              _buildDiamondLogo(context),
              const SizedBox(height: 32),
              // App name
              Text(
                'CRYPTO WALLET',
                style: AppTextStyles.heading1.copyWith(
                  color: AppColors.gunmetal,
                  fontWeight: FontWeight.w800,
                  letterSpacing: 2,
                ),
              ),
              const SizedBox(height: 16),
              // Tagline
              Text(
                'Secure. Simple. Smart.',
                style: AppTextStyles.body.copyWith(
                  color: AppColors.gunmetal.withOpacity(0.7),
                ),
                textAlign: TextAlign.center,
              ),
              const Spacer(flex: 2),
              // START NOW button
              _buildStartNowButton(context),
              const SizedBox(height: 16),
              // LOGIN option
              _buildLoginOption(context),
              const Spacer(flex: 1),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildDiamondLogo(BuildContext context) {
    return FlatContainer(
      padding: const EdgeInsets.all(24),
      borderRadius: 20,
      backgroundColor: Colors.white,
      elevation: 0,
      child: Container(
        width: 120,
        height: 120,
        decoration: const BoxDecoration(
          color: AppColors.earthYellow,
          shape: BoxShape.circle,
        ),
        child: Transform.rotate(
          angle: 0.785398, // 45 degrees in radians
          child: const Icon(
            Icons.stop,
            size: 80,
            color: Colors.white,
          ),
        ),
      ),
    );
  }

  Widget _buildStartNowButton(BuildContext context) {
    return SizedBox(
      width: double.infinity,
      height: 60,
      child: CustomButton(
        onPressed: () {
          Navigator.of(context).pushReplacement(
            MaterialPageRoute(
              builder: (context) => const DashboardScreen(),
            ),
          );
        },
        borderRadius: 15,
        backgroundColor: AppColors.accent,
        child: Center(
          child: Text(
            'START NOW',
            style: AppTextStyles.button.copyWith(
              color: Colors.white,
              fontWeight: FontWeight.bold,
              letterSpacing: 1.5,
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildLoginOption(BuildContext context) {
    return TextButton(
      onPressed: () {
        // Login functionality would go here
        // For now, we'll navigate to the dashboard
        Navigator.of(context).pushReplacement(
          MaterialPageRoute(
            builder: (context) => const DashboardScreen(),
          ),
        );
      },
      child: Text(
        'LOGIN',
        style: AppTextStyles.button.copyWith(
          color: AppColors.amaranthPurple,
          fontWeight: FontWeight.w600,
        ),
      ),
    );
  }
} 