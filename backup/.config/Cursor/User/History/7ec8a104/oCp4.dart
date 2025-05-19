import 'package:flutter/widgets.dart';
import 'package:flutter_neumorphic_plus/flutter_neumorphic.dart';
import '../constants/colors.dart';

class NeumorphicContainer extends StatelessWidget {
  final Widget child;
  final EdgeInsetsGeometry padding;
  final double borderRadius;
  final bool isPressed;
  final double depth;
  final NeumorphicStyle? style;

  const NeumorphicContainer({
    super.key,
    required this.child,
    this.padding = const EdgeInsets.all(16),
    this.borderRadius = 15,
    this.isPressed = false,
    this.depth = 8,
    this.style,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: AppColors.background,
        borderRadius: BorderRadius.circular(borderRadius),
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
      child: Padding(
        padding: padding,
        child: child,
      ),
    );
  }
}

class CustomNeumorphicButton extends StatelessWidget {
  final VoidCallback onPressed;
  final Widget child;
  final EdgeInsets? padding;
  final double borderRadius;
  final Color? backgroundColor;
  final bool isAccent;

  const CustomNeumorphicButton({
    super.key,
    required this.onPressed,
    required this.child,
    this.padding = const EdgeInsets.symmetric(vertical: 16, horizontal: 24),
    this.borderRadius = 15,
    this.backgroundColor,
    this.isAccent = false,
  });

  @override
  Widget build(BuildContext context) {
    return NeumorphicButton(
      onPressed: onPressed,
      style: NeumorphicStyle(
        depth: 8,
        intensity: 0.8,
        surfaceIntensity: 0.2,
        color: isAccent ? AppColors.accent : (backgroundColor ?? AppColors.background),
        boxShape: NeumorphicBoxShape.roundRect(BorderRadius.circular(borderRadius)),
      ),
      padding: padding,
      child: child,
    );
  }
}

class NeumorphicIcon extends StatelessWidget {
  final IconData icon;
  final Color? color;
  final double size;
  final bool isPressed;

  const NeumorphicIcon({
    super.key,
    required this.icon,
    this.color,
    this.size = 24,
    this.isPressed = false,
  });

  @override
  Widget build(BuildContext context) {
    return Neumorphic(
      style: NeumorphicStyle(
        depth: isPressed ? -4 : 4,
        boxShape: const NeumorphicBoxShape.circle(),
        color: AppColors.background,
      ),
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Icon(
          icon,
          color: color ?? AppColors.textPrimary,
          size: size,
        ),
      ),
    );
  }
} 