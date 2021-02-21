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
        "p:first-child {\n" +
        "  text-indent: " + 16 + "px;\n" +
        "}" +
        "*:last-child {\n" +
        "  margin-bottom: 0px;\n" +
        "}\n" +
        "* {\n" +
        "  color: rgb(89 89 89);\n" +
        "}" +
        "</style>" +
        "<p>" +
        comment.text, "text/html", "UTF-8");

        vCommentLenReply.setText(
            Integer.toString(
                comment.kids == null ? 0 : comment.kids.length
            )
        );

        Log.v("test", "  text-indent: " + context.getResources().getDimensionPixelSize(R.dimen.action_space_for_icon) + "px;\n");

        vCommentAgo.setText("| " + comment.getTimeAgo() + " |");
        vCommentBy.setText("by " + comment.by);

        return vComment;
    }

    public static int spToPx(float sp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }
}
