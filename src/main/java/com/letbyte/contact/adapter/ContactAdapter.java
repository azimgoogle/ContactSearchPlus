package com.letbyte.contact.adapter;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.letbyte.contact.BR;
import com.letbyte.contact.R;
import com.letbyte.contact.data.model.Contact;
import com.letbyte.contact.databinding.ContactBinding;
import com.letbyte.contact.drawable.CircleTransform;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nuc on 7/28/2015.
 */
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.BindingHolder> {

    public static final class BindingHolder extends RecyclerView.ViewHolder {
        private ViewDataBinding binding;

        public BindingHolder(View rowView) {
            super(rowView);
            binding = DataBindingUtil.bind(rowView);
        }

        public ViewDataBinding getBinding() {
            return binding;
        }
    }

    private List<Contact> contacts;
    private int viewResourceId;
    private Handler handler;

    public ContactAdapter(int viewResourceId, List<Contact> contacts) {
        this.viewResourceId = viewResourceId;
        this.contacts = new ArrayList<>(contacts);
        handler = new Handler();
    }

    public Contact getItem(int position) {
        return contacts.get(position);
    }

    public void adds(List<Contact> contacts) {
        this.contacts = new ArrayList<>(contacts);
        postNotify();
    }

    private void postNotify() {

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        }, 1000);
    }

    public void applyTo(List<Contact> contacts) {
        if(contacts.size() > 100) {//More intelligence can be applied
        // by measuring time difference of notify data set changes
            this.contacts.clear();
            this.contacts.addAll(contacts);
            notifyDataSetChanged();
        } else {
//            long t1 = System.currentTimeMillis();
            applyToRemove(contacts);
            applyToAdd(contacts);
            applyToMove(contacts);

            postNotify();
//            long t2 = System.currentTimeMillis();
        }
    }

    private void addItem(int position, Contact contact) {
        contacts.add(position, contact);
//        if (getItemCount() > 100) return;
        notifyItemInserted(position);
    }

    private void moveItem(int fromPosition, int toPosition) {
        final Contact contact = contacts.remove(fromPosition);
        contacts.add(toPosition, contact);
//        if (getItemCount() > 100) return;
        notifyItemMoved(fromPosition, toPosition);
    }

    private void removeItem(int position) {
        contacts.remove(position);
//        if (getItemCount() > 100) return;
        notifyItemRemoved(position);
    }


    private void applyToAdd(List<Contact> newContacts) {
        for (int i = 0, count = newContacts.size(); i < count; i++) {
            final Contact model = newContacts.get(i);
            if (!contacts.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyToMove(List<Contact> newContacts) {
        for (int toPosition = newContacts.size() - 1; toPosition >= 0; toPosition--) {
            final Contact model = newContacts.get(toPosition);
            final int fromPosition = contacts.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    private void applyToRemove(List<Contact> newContacts) {
        for (int i = contacts.size() - 1; i >= 0; i--) {
            final Contact model = contacts.get(i);
            if (!newContacts.contains(model)) {
                removeItem(i);
            }
        }
    }


    @Override
    public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowView = LayoutInflater.from(parent.getContext()).inflate(viewResourceId, parent, false);
        return new BindingHolder(rowView);
    }

    @Override
    public void onBindViewHolder(BindingHolder holder, int position) {
        final Contact contact = getItem(position);

        if (contact.getImageUri() != null) {
            Picasso.with(holder.getBinding().getRoot().getContext())
                    .load(contact.getImageUri()).transform(new CircleTransform())
                    .resize(55, 55)
                    .centerCrop()
                    .into(((ContactBinding) holder.getBinding()).imgIcon);
/*
         //   File file = new File(contact.getImageUri());

            Uri uri = Uri.parse(contact.getImageUri());

            Control.log("Uriii >>> " + uri.getPath());

            ((com.letbyte.contact.databinding.ContactBinding) holder.getBinding()).simpleDraweeView.setImageURI(uri);
*/
        } else {
            ((ContactBinding) holder.getBinding()).imgIcon.setImageResource(R.mipmap.ic_launcher);
        }


        holder.getBinding().setVariable(BR.contact, contact);
        holder.getBinding().executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public long getContactIDbyPosition(int position) {
        long cID = contacts.get(position).getId();
        return cID;
    }
}
