package com.dgensolutions.freesky.freesky;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity;

/**
 * Created by Ganesh Kaple on 17-09-2016.
 */
public class ChatFragment extends Fragment{

    Button mChatStart;
    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Intent intent = new Intent(getActivity(), ConversationActivity.class);
        if(ApplozicClient.getInstance(getActivity()).isContextBasedChat()){
            intent.putExtra(ConversationUIService.CONTEXT_BASED_CHAT,true);
        }
        startActivity(intent);
        return;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View view =inflater.inflate(R.layout.fragment_chat, container, false);
        mChatStart = (Button) view.findViewById(R.id.initiateChatButton);
        mChatStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ConversationActivity.class);
                if(ApplozicClient.getInstance(getActivity()).isContextBasedChat()){
                    intent.putExtra(ConversationUIService.CONTEXT_BASED_CHAT,true);
                }
                startActivity(intent);
            }
        });
        return view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // If the drawer is open, show the global app actions in the action bar. See also
        // showGlobalContextActionBar, which controls the top-left area of the action bar.

        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_example) {
            Intent intent = new Intent(getActivity(), ConversationActivity.class);
            if(ApplozicClient.getInstance(getActivity()).isContextBasedChat()){
                intent.putExtra(ConversationUIService.CONTEXT_BASED_CHAT,true);
            }
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}


