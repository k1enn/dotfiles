import 'package:flutter/material.dart';
import '../models/post.dart';
import 'post_card.dart';

class PostSection extends StatelessWidget {
  final List<Post> posts;
  const PostSection({
    super.key,
    required this.posts,
  });

  @override
  Widget build(BuildContext context) {
    return Padding(
        padding: const EdgeInsets.all(8.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Padding(
                padding: EdgeInsets.only(
                  left: 16.0,
                  bottom: 8.0,
                ),
                child: Text(
                  'Friends\'s Activity',
                  style: TextStyle(
                    fontSize: 24,
                    fontWeight: FontWeight.bold,
                  ),
                )),
            ListView.separated(
// 2
              primary: false,
// 3
              shrinkWrap: true,
// 4
              scrollDirection: Axis.vertical,
// 5
              physics: const NeverScrollableScrollPhysics(),
              itemCount: posts.length,
// 6
              itemBuilder: (context, index) {
                return PostCard(post: posts[index]);
              },
              separatorBuilder: (context, index) {
// 7
                return const SizedBox(height: 16);
              },
            )
          ],
        ));
  }
}
