import 'package:flutter/material.dart';
import '../models/post.dart';

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
            padding: EdgeInsets.only(left: 16.0, bottom: 8.0,),
            child: Text(
              'Friends\'s Activity',
              style: TextStyle(fontSize:24,fontWeight: FontWeight.bold,),
            )
          ),
        //TODO: Add Post List View
        ],
      )

    );
  }
}