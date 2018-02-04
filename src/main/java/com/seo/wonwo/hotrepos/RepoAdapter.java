package com.seo.wonwo.hotrepos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Adapter to populate items in ListView with custom layout.
 */
public class RepoAdapter extends BaseAdapter {
    private Context viewContext;
    private LayoutInflater viewInflater;
    private String[] titleSource;
    private String[] subtitleSource;
    private String[] descriptionSource;

    /**
     * Constructor of adapter. Gets list ot strings to set in TextViews as arguments.
     * @param context View context of caller.
     * @param titleList List containing names of repositories.
     * @param subtitleList List containing owners of repositories.
     * @param descList List containing descriptions of repositories.
     */
    public RepoAdapter(Context context, String[] titleList, String[] subtitleList, String[] descList) {
        viewContext = context;
        titleSource = titleList;
        subtitleSource = subtitleList;
        descriptionSource = descList;
        viewInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return titleSource.length;
    }

    @Override
    public Object getItem(int index) {
        return titleSource[index];
    }

    /**
     * ViewHolder to hold pointers to TextViews. For recycle / reuse purpose.
     */
    static class ViewHolder {
        TextView titleView;
        TextView subtitleView;
        TextView descriptionView;
    }

    @Override
    public View getView(int index, View itemView, ViewGroup parent) {
        final ViewHolder holder;
        if(itemView == null) { // This is first time view is being created
            itemView = viewInflater.inflate(R.layout.repo_list_item, parent, false);
            holder = new ViewHolder();
            holder.titleView = itemView.findViewById(R.id.repolist_item_title);
            holder.subtitleView = itemView.findViewById(R.id.repolist_item_subtitle);
            holder.descriptionView = itemView.findViewById(R.id.repolist_item_description);
            itemView.setTag(holder);
        } else { // View has already been created; use holder to avoid calling findViewById() all again
            holder = (ViewHolder) itemView.getTag();
        }
        holder.titleView.setText(titleSource[index]);
        holder.subtitleView.setText(subtitleSource[index]);
        holder.descriptionView.setText(descriptionSource[index]);
        return itemView;
    }

    @Override
    public long getItemId(int index) {
        return index;
    }
}