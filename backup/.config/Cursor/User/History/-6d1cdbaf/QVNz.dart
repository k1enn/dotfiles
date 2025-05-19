import 'package:flutter_neumorphic/flutter_neumorphic.dart';
import 'package:provider/provider.dart';
import 'constants/colors.dart';
import 'providers/crypto_provider.dart';
import 'screens/splash_screen.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => CryptoProvider()),
      ],
      child: NeumorphicApp(
        debugShowCheckedModeBanner: false,
        title: 'Crypto Wallet',
        themeMode: ThemeMode.light,
        theme: NeumorphicThemeData(
          baseColor: AppColors.background,
          lightSource: LightSource.topLeft,
          depth: 10,
          intensity: 0.5,
          textTheme: TextTheme(
            titleLarge: TextStyle(
              color: AppColors.textPrimary,
              fontWeight: FontWeight.bold,
            ),
            bodyLarge: TextStyle(color: AppColors.textPrimary),
            bodyMedium: TextStyle(color: AppColors.textPrimary),
          ),
        ),
        home: const SplashScreen(),
      ),
    );
  }
}
