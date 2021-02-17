package com.example.hackernews;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.zip.Inflater;

public class CommentItem {
    @SuppressLint("SetTextI18n")
    static public View inflateCommentTemplate(
        LayoutInflater inf, ViewGroup parent, Comment comment
    ) {
        View vComment = inf.inflate(R.layout.new_comment, parent, false);
        TextView vCommentText     = vComment.findViewById(R.id.comment_text);
        TextView vCommentLenReply = vComment.findViewById(R.id.comment_len_reply);
        TextView vCommentAgo      = vComment.findViewById(R.id.comment_ago);
        TextView vCommentBy       = vComment.findViewById(R.id.comment_by);

        vCommentText.setText(comment.text);
        vCommentLenReply.setText(Integer.toString(comment.kids.length));
        vCommentAgo.setText("| " + comment.getTimeAgo() + " |");
        vCommentBy.setText("by " + comment.by);

        return vComment;
    }
}
