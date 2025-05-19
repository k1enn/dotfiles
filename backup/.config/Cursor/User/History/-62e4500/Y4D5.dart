import 'package:flutter/material.dart';
import '../api/mock_yummy_service.dart';

class ExplorePage extends StatelessWidget {
// 1
  final mockService = MockYummyService();
  ExplorePage({super.key});

  @override
  Widget build(BuildContext context) {
    return FutureBuilder(
        future: mockService.getExploreData(),
        builder: (context, AsyncSnapshot<ExploreData> snapshots) {
          if (snapshots.connectionState == ConnectionState.done) {
            final restaurants = snapshots.data?.restaurants ?? [];
            final categories = snapshots.data?.categories ?? [];
            final posts = snapshots.data?.friendPosts ?? [];
            // TODO: Replace this with RestaurantSection
            return const Center(
              child: SizedBox(
                child: Text('Show RestaurentSecion'),
              ),
            );
          } else {
            return const Center(
              child: CircularProgressIndicator(),
            );
          }
        });
// 2
  }
}
