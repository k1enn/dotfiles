import 'package:flutter/material.dart';
import 'package:flutter_neumorphic/flutter_neumorphic.dart';
import '../constants/colors.dart';
import '../constants/text_styles.dart';
import '../widgets/neumorphic_container.dart';
import 'dashboard_screen.dart';

class SplashScreen extends StatelessWidget {
  const SplashScreen({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.background,
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
    return NeumorphicContainer(
      padding: const EdgeInsets.all(24),
      borderRadius: 20,
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
      child: NeumorphicButton(
        onPressed: () {
          Navigator.of(context).pushReplacement(
            MaterialPageRoute(
              builder: (context) => const DashboardScreen(),
            ),
          );
        },
        style: NeumorphicStyle(
          depth: 8,
          intensity: 0.8,
          boxShape: NeumorphicBoxShape.roundRect(BorderRadius.circular(15)),
          color: AppColors.accent,
        ),
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