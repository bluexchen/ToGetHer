package com.exfantasy.together.components.cardView;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.exfantasy.together.R;


/**
 * Created by User on 2015/12/11.
 */
public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder>{
    private MsgRecordItem[] msgRecordItems;

    public CardAdapter(MsgRecordItem[] msgRecordItems) {this.msgRecordItems = msgRecordItems;}

    @Override
    public CardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_item, null);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CardAdapter.ViewHolder viewHolder, int position) {
        viewHolder.tvMessage.setText(msgRecordItems[position].getCreateName()+":"+msgRecordItems[position].getContent());
    }

    @Override
    public int getItemCount() {
        return msgRecordItems.length;
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        public TextView tvMessage;

        public ViewHolder(View itemView) {
            super(itemView);
            tvMessage = (TextView)itemView.findViewById(R.id.tv_message_at_cardview_item);
        }
    }
}


