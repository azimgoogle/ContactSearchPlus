package com.letbyte.contact.adapter;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;

import com.letbyte.contact.BR;
import com.letbyte.contact.R;
import com.letbyte.contact.control.Constant;
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

    public static boolean IS_SCROLLING_IDLE = true;

    public static final class BindingHolder extends RecyclerView.ViewHolder {
        private ViewDataBinding binding;
        public String imageUri;
        public ImageView imageView;

        public BindingHolder(View rowView) {
            super(rowView);
            binding = DataBindingUtil.bind(rowView);
        }

        public ViewDataBinding getBinding() {
            return binding;
        }
    }

    private List<Contact> contactsToView, contactsOriginal;
    private int viewResourceId;
    private Handler handler;

    public ContactAdapter(int viewResourceId, List<Contact> contacts) {
        this.viewResourceId = viewResourceId;
        this.contactsOriginal = contacts;
        this.contactsToView = new ArrayList<>(contacts);
        handler = new Handler();
    }

    private ModelFilter filter;

    public Filter getFilter() {
        if (filter == null)
            filter = new ModelFilter();
        return filter;
    }

    public Contact getItem(int position) {
        return contactsToView.get(position);
    }

    public void adds(List<Contact> contacts) {
        this.contactsToView = new ArrayList<>(contacts);
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
        if (contacts.size() > 100) {//More intelligence can be applied
            // by measuring time difference of notify data set changes
            this.contactsToView.clear();
            this.contactsToView.addAll(contacts);
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
        contactsToView.add(position, contact);
//        if (getItemCount() > 100) return;
        notifyItemInserted(position);
    }

    private void moveItem(int fromPosition, int toPosition) {
        final Contact contact = contactsToView.remove(fromPosition);
        contactsToView.add(toPosition, contact);
//        if (getItemCount() > 100) return;
        notifyItemMoved(fromPosition, toPosition);
    }

    private void removeItem(int position) {
        contactsToView.remove(position);
//        if (getItemCount() > 100) return;
        notifyItemRemoved(position);
    }


    private void applyToAdd(List<Contact> newContacts) {
        for (int i = 0, count = newContacts.size(); i < count; i++) {
            final Contact model = newContacts.get(i);
            if (!contactsToView.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyToMove(List<Contact> newContacts) {
        for (int toPosition = newContacts.size() - 1; toPosition >= 0; toPosition--) {
            final Contact model = newContacts.get(toPosition);
            final int fromPosition = contactsToView.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    private void applyToRemove(List<Contact> newContacts) {
        for (int i = contactsToView.size() - 1; i >= 0; i--) {
            final Contact model = contactsToView.get(i);
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
        holder.imageUri = contact.getImageUri();
        holder.getBinding().setVariable(BR.contact, contact);
        holder.getBinding().executePendingBindings();
    }

    @Override
    public void onViewAttachedToWindow(BindingHolder holder) {
        super.onViewAttachedToWindow(holder);
        String imageUri = holder.imageUri;
        ImageView imageView = ((ContactBinding) holder.getBinding()).imgIcon;
        holder.imageView = imageView;
        if (imageUri != null && IS_SCROLLING_IDLE) {
            Picasso.with(holder.getBinding().getRoot().getContext())
                    .load(imageUri).transform(new CircleTransform())
                    .resize(55, 55)
                    .centerCrop()
                    .into(imageView);

            //   File file = new File(contact.getImageUri());

            /*Uri uri = Uri.parse(contact.getImageUri());

            Control.log("Uriii >>> " + uri.getPath());

            ((com.letbyte.contact.databinding.ContactBinding) holder.getBinding()).simpleDraweeView.setImageURI(uri);*/

        } else {
//            imageView.setImageResource(R.drawable.ic_account_circle_24dp);
            imageView.setImageResource(R.drawable.ic_account_circle_24dp);
        }
    }

    @Override
    public void onViewDetachedFromWindow(BindingHolder holder) {
        super.onViewDetachedFromWindow(holder);
        Picasso.with(holder.getBinding().getRoot().getContext()).cancelRequest(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return contactsToView.size();
    }

    public long getContactIDbyPosition(int position) {
        long cID = contactsToView.get(position).getId();
        return cID;
    }

    //Make a filter command to cancel previous tasks where user write query text too early
    // before finishing existing filter task
    private class ModelFilter extends Filter
    {
        private String st = null, oldString = Constant.EMPTY_STRING;
        private final int[] searchIndexes = new int[]{
                Constant.DISPLAY_NAME,
                Constant.PHONE_NUMBER,
                Constant.MAIL_ADDRESS,
                Constant.ADDRESS,
                Constant.NOTES,
                Constant.ORGANIZATION,
                Constant.RELATION};
        private boolean isThereNewQueryText;//volatile??

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String filterString = constraint.toString(), originalFilterString = filterString;
            st = filterString;
            FilterResults results = new FilterResults();
            final List<Contact> list;
            final List<Contact> nList;

            if(constraint == null || constraint.length() == 0) {
                nList = contactsOriginal;
                Spannable spannable = Spannable.Factory.getInstance().newSpannable(Constant.EMPTY_STRING);
                for (Contact contactModel : nList) {
                    contactModel.setSubTextSpanned(spannable);
                }
            } else {
                list = originalFilterString.length() > oldString.length() ? contactsToView : contactsOriginal;
                final int count = list.size();
                nList = new ArrayList<>(count);
                filterString = filterString.toLowerCase();
                Contact contactModel;
                List<String> searchList;
                boolean isMatched;
                int indexOfSubString;
                String subString;
                for (int i = 0; i < count; i++) {
                    contactModel = list.get(i);
                    isMatched = false;
                    for (int index : searchIndexes) {
                        searchList = contactModel.getDataIndicesByDataIndex(index);
                        for (String value : searchList) {
                            indexOfSubString = value.indexOf(filterString);
                            if (indexOfSubString != -1) {
                                isMatched = true;
                                if(index != Constant.DISPLAY_NAME) {//If display name then manipulate display name particularly

                                    subString = value.substring(0, indexOfSubString);
                                    subString +=  originalFilterString ;
                                    subString += value.substring(indexOfSubString + originalFilterString.length(), value.length());
                                    SpannableString spannableString = new SpannableString(subString);
                                    spannableString.setSpan(new StyleSpan(Typeface.BOLD), indexOfSubString,
                                            indexOfSubString + originalFilterString.length(), 0);
                                    contactModel.setSubTextSpanned(spannableString);
                                }
                                nList.add(contactModel);
                                break;
                            }
                        }
                        if (isMatched)
                            break;
                    }
                }
            }


            results.values = nList;
            results.count = nList.size();
            oldString = originalFilterString;
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            contactsToView = (ArrayList<Contact>) results.values;
            notifyDataSetChanged();
        }
    }
}
