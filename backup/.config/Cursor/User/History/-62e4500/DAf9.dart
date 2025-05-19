import 'package:flutter/material.dart';
import '../api/mock_yummy_service.dart';
class ExplorePage extends StatelessWidget {
// 1
final mockService = MockYummyService();
ExplorePage({super.key});
}
@override
Widget build(BuildContext context) {
// TODO: Add Listview Future Builder
// 2
return const Center(
child: Text('Explore Page Setup',
style: TextStyle(fontSize: 32.0),),);