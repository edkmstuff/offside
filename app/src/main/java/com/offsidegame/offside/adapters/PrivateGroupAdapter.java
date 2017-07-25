package com.offsidegame.offside.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.offsidegame.offside.R;
import com.offsidegame.offside.helpers.ImageHelper;
import com.offsidegame.offside.helpers.RoundImage;
import com.offsidegame.offside.models.OffsideApplication;
import com.offsidegame.offside.models.PrivateGroup;
import com.offsidegame.offside.models.PrivateGroupPlayer;

import org.acra.ACRA;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by user on 7/20/2017.
 */

public class PrivateGroupAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<PrivateGroup> privateGroups;


    public PrivateGroupAdapter(Context context, ArrayList<PrivateGroup> privateGroups) {
        this.context = context;
        this.privateGroups = privateGroups;
    }

    @Override
    public int getCount() {
        return privateGroups.size();
    }

    @Override
    public PrivateGroup getItem(int position) {
        return privateGroups.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder {

        PrivateGroup privateGroup;
        TextView groupNameTextView;
        TextView groupGameStatusTextView;

        TextView totalPlayingPlayersInGroupTextView;
        LinearLayout playersPlayInGroupRoot;


    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            PrivateGroupAdapter.ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.private_group_item_1, parent, false);
                viewHolder = new PrivateGroupAdapter.ViewHolder();

                //viewHolder.incomingProfilePictureImageView = (ImageView) convertView.findViewById(R.id.cm_incoming_profile_picture_image_view);
                viewHolder.groupNameTextView = (TextView) convertView.findViewById(R.id.pg_group_name_text_view);
                viewHolder.groupGameStatusTextView = (TextView) convertView.findViewById(R.id.pg_group_game_status_text_view);
                viewHolder.playersPlayInGroupRoot = (LinearLayout) convertView.findViewById(R.id.pg_players_play_in_group_root);
                viewHolder.totalPlayingPlayersInGroupTextView = (TextView) convertView.findViewById(R.id.pg_total_playing_players_in_group_text_view);
                viewHolder.playersPlayInGroupRoot = (LinearLayout) convertView.findViewById(R.id.pg_players_play_in_group_root);

                convertView.setTag(viewHolder);


            } else
                viewHolder = (PrivateGroupAdapter.ViewHolder) convertView.getTag();

            viewHolder.privateGroup = getItem(position);
            if (viewHolder.privateGroup == null)
                return convertView;

            viewHolder.groupNameTextView.setText(viewHolder.privateGroup.getName());


            if (viewHolder.privateGroup.isActive()) {

                viewHolder.playersPlayInGroupRoot.removeAllViews();

                PrivateGroupPlayer[] players = viewHolder.privateGroup.getPrivateGroupPlayers();

                Arrays.sort(players, new Comparator<PrivateGroupPlayer>() {
                    public int compare(PrivateGroupPlayer p1, PrivateGroupPlayer p2) {
                        return  p1.getDisplayPriority() >= p2.getDisplayPriority() ? -1 :  p1.getDisplayPriority() < p2.getDisplayPriority()? 1  : 0;
                    }
                });



                for(PrivateGroupPlayer privateGroupPlayer: players){

                    ViewGroup playerLayout = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.player_playing_in_private_group_item, viewHolder.playersPlayInGroupRoot,false);
                    ImageView playerImageImageView = (ImageView) playerLayout.getChildAt(0);
                    playerImageImageView.getLayoutParams().height = 70;
                    playerImageImageView.getLayoutParams().width = 70;
                    playerImageImageView.requestLayout();
                    String imageUrl = privateGroupPlayer.getImageUrl() == null || privateGroupPlayer.getImageUrl().equals("")  ? OffsideApplication.getDefaultProfilePictureUrl(): privateGroupPlayer.getImageUrl();
                    Uri imageUri = Uri.parse(imageUrl);
                    ImageHelper.loadImage(context,playerImageImageView,imageUri);

                    View isActiveIndicatorImageView = (View) playerLayout.getChildAt(1);
                    //ImageHelper.loadImage(context,isActiveIndicatorImageView,R.drawable.shape_bg_circle_active);

                    if(privateGroupPlayer.getActive().booleanValue())
                        isActiveIndicatorImageView.setVisibility(View.VISIBLE);
                    else
                        isActiveIndicatorImageView.setVisibility(View.GONE);



                    viewHolder.playersPlayInGroupRoot.addView(playerLayout);
                }


                int countActivePlayersInPrivateGroup = viewHolder.privateGroup.getActivePlayersCount();
                viewHolder.totalPlayingPlayersInGroupTextView.setText(Integer.toString(countActivePlayersInPrivateGroup) +" "+context.getString(R.string.lbl_now_playing));
                viewHolder.groupGameStatusTextView.setText(R.string.lbl_game_is_active);
                //viewHolder.groupGameStatusTextView.setVisibility(View.VISIBLE);
            } else {
                viewHolder.totalPlayingPlayersInGroupTextView.setText(R.string.lbl_no_playing_players);
                //viewHolder.groupGameStatusTextView.setVisibility(View.GONE);
                viewHolder.groupGameStatusTextView.setText(R.string.lbl_start_a_game);
                viewHolder.groupGameStatusTextView.setBackgroundResource(R.color.navigationMenuSelectedItem);

            }

            convertView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    //Toast.makeText(context,"item clicked" ,Toast.LENGTH_SHORT).show();
                    //ToDo: open the group detailed activity
                }
            });





            return convertView;

        } catch (Exception ex) {
            ACRA.getErrorReporter().handleSilentException(ex);

        }

        return null;

    }


}
