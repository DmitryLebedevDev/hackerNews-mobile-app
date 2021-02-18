package com.example.hackernews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.LeadingMarginSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import java.util.zip.Inflater;

public class CommentItem {
    @SuppressLint("SetTextI18n")
    static public View inflateCommentTemplate(
        LayoutInflater inf, ViewGroup parent, Comment comment, Context context
    ) {
        View vComment = inf.inflate(R.layout.new_comment, parent, false);
        WebView vCommentText     = vComment.findViewById(R.id.comment_text);
        TextView vCommentLenReply = vComment.findViewById(R.id.comment_len_reply);
        TextView vCommentAgo      = vComment.findViewById(R.id.comment_ago);
        TextView vCommentBy       = vComment.findViewById(R.id.comment_by);

        vCommentText.loadData( "<style>" +
                "body {\n" +
                "  margin: 0px;\n" +
                "}" +
                "body > *:first-child {\n" +
                "  text-indent: 100px\n" +
                "}" +
                "p:first-child {\n" +
                "  text-indent: 20px\n" +
                "}" +
                "p:last-child {\n" +
                "  margin-bottom: 0px;\n" +
                "}" +
                "* {\n" +
                "  color: rgb(89 89 89);\n" +
                "  margin-left: 0px;" +
                "}" +
                "</style>" +
                "<p>" +
                comment.text, "text/html; charset=utf-8", "utf-8");

        Log.v("test", "<span style=\"width: " + spToPx(16, context) + "px\"></span>");
        vCommentLenReply.setText(Integer.toString(comment.kids.length));
        vCommentAgo.setText("| " + comment.getTimeAgo() + " |");
        vCommentBy.setText("by " + comment.by);

        return vComment;
    }

    public static int spToPx(float sp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }
}
