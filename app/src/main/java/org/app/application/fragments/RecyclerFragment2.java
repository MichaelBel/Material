/*
 * Copyright 2015 Michael Bel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.app.application.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.app.application.R;
import org.app.application.cells.RecyclerCell;
import org.app.application.model.RecItem;
import org.app.material.widget.LayoutHelper;
import org.app.material.widget.RecyclerListView;

import java.util.ArrayList;

public class RecyclerFragment2 extends Fragment {

    private ArrayList<RecItem> items;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup view, Bundle savedInstanceState) {
        FrameLayout fragmentView = new FrameLayout(getActivity());
        fragmentView.setBackgroundColor(0xFFF0F0F0);

        items = new ArrayList<>();
        items.add(new RecItem(1, R.drawable.space1, "1. Primary text", "Secondary text"));
        items.add(new RecItem(2, R.drawable.space2, "2. Primary text", "Secondary text"));
        items.add(new RecItem(3, R.drawable.space3, "3. Primary text", "Secondary text"));
        items.add(new RecItem(4, R.drawable.space4, "4. Primary text", "Secondary text"));
        items.add(new RecItem(5, R.drawable.space5, "5. Primary text", "Secondary text"));
        items.add(new RecItem(6, R.drawable.space6, "6. Primary text", "Secondary text"));
        items.add(new RecItem(7, R.drawable.space1, "7. Primary text", "Secondary text"));
        items.add(new RecItem(8, R.drawable.space2, "8. Primary text", "Secondary text"));
        items.add(new RecItem(9, R.drawable.space3, "9. Primary text", "Secondary text"));

        RecyclerViewAdapter adapter = new RecyclerViewAdapter(getContext());

        RecyclerListView recyclerView = new RecyclerListView(getActivity());
        recyclerView.setFocusable(true);
        //recyclerView.setTag(7);
        recyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager;
        layoutManager = new LinearLayoutManager(getContext()) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        };
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(null);
        recyclerView.setInstantClick(true);
        recyclerView.setLayoutAnimation(null);
        recyclerView.setLayoutParams(LayoutHelper.makeFrame(getActivity(), LayoutHelper.MATCH_PARENT,
                LayoutHelper.MATCH_PARENT));
        recyclerView.setOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                RecItem item = items.get(position);
                Toast.makeText(getContext(), "Item = " + item.getId(), Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setOnItemLongClickListener(new RecyclerListView.OnItemLongClickListener() {
            @Override
            public boolean onItemClick(View view, int position) {
                RecItem item = items.get(position);
                Toast.makeText(getContext(), "Item = " + item.getId(), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        fragmentView.addView(recyclerView);

        return fragmentView;
    }

    public class RecyclerViewAdapter extends RecyclerListView.Adapter {
        private Context mContext;

        public RecyclerViewAdapter(Context context) {
            this.mContext = context;
        }

        private class Holder extends RecyclerView.ViewHolder {

            public Holder(View view) {
                super(view);

                ((RecyclerCell) view).setOnOptionsClick(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle(R.string.Options);
                        builder.setItems(new CharSequence[]{
                                getString(R.string.Open),
                                getString(R.string.Remove)
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0) {
                                    final RecItem item = items.get(getAdapterPosition());
                                    Toast.makeText(mContext, getString(R.string.OpeningItem, item.getId()),
                                            Toast.LENGTH_SHORT).show();
                                } else if (i == 1) {
                                    items.remove(getAdapterPosition());
                                    notifyItemRemoved(getAdapterPosition());
                                    notifyItemRangeChanged(getAdapterPosition(), items.size());
                                }
                            }
                        });
                        builder.show();
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup group, final int type) {
            View view;

            view = new RecyclerCell(mContext);
            view.setBackgroundColor(0xFFFFFFFF);
            view.setBackgroundResource(R.drawable.list_selector_white);
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT));

            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            RecItem item = items.get(position);

            ((RecyclerCell) viewHolder.itemView)
                    .setImage(item.getImage())
                    .setText1(item.getText1())
                    .setText2(item.getText2())
                    .withDivider(true);
        }
    }
}