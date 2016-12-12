package edu.wisc.meetme;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import static java.util.Arrays.asList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;






    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragment_preference, container, false);


        final SharedPreferences sharedPreferences = getActivity().getSharedPreferences("edu.wisc.meetme", Context.MODE_PRIVATE);


       final ArrayList<String> foodOption = new ArrayList<String>(asList(
               "Afghan",
                "African",
                "Ethiopian",
                "American",
                "Asian",
                "Burmese",
                "Cambodian",
                "Chinese",
                "Cantoness",
                "Dim Sum",
                "Fujian",
                "Hunan",
                "Peking Duck",
                "Shanghai",
                "Szechuan",
                "Taiwanese",
                "Filipino",
                "Himalayan",
                "Hotpot",
                "Indonesian",
                "Japanese",
                "Ramen",
                "Soba",
                "Sushi",
                "Udon",
                "Korean",
                "Malay",
                "Mongolian",
                "Noodle House",
                "Thai",
                "Tibetan",
                "Vietnamese",
                "Australian",
                "BBQ",
                "Bakery",
                "Breakfast",
                "Bubble Tea",
                "Buffet",
                "Burgers",
                "Cajun/Creole",
                "Caribbean",
                "Coffee",
                "Creperie",
                "Desserts",
                "Frozen Yogurt",
                "Ice Cream",
                "Diner",
                "Donuts",
                "English",
                "Falafel",
                "Fast Food",
                "Food Truck",
                "French",
                "Fried Chicken",
                "German",
                "Greek",
                "Hot Dogs",
                "Indian",
                "North Indian",
                "South Indian",
                "Irish",
                "Italian",
                "Latin American",
                "Mac & Cheese",
                "Mediterranean",
                "Mexican",
                "Burritos",
                "Tacos",
                "Tex-Mex",
                "Middle Eastern",
                "Pizza",
                "Portuguese",
                "Poutine",
                "Sandwiches",
                "Seafood",
                "Southern/Soul",
                "Spanish",
                "Sri Lankan",
                "Steakhouse",
                "Vegetarian/Vegan",
                "Wings"
       ));


        ListView myListView = (ListView) view.findViewById(R.id.foodList);

        myListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);


//        ArrayList<String> myFood = new ArrayList<String>();
        ArrayAdapter<String> listViewAdapater = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_list_item_single_choice,
                foodOption
        );
        myListView.setAdapter(listViewAdapater);

        final ArrayList<String> rememberFood = new ArrayList<>();

        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                CheckedTextView checkedTextView = (CheckedTextView) view;

                if (checkedTextView.isChecked()) {
                    Log.i("Info", foodOption.get(position) + " is selected.");
                    rememberFood.add(foodOption.get(position));
                } else {
                    Log.i("Info", foodOption.get(position) + " is deselected.");
                    rememberFood.remove(foodOption.get(position));
                }
                Log.i("Info", rememberFood.toString());

                try {
                    sharedPreferences.edit().putString("FoodPreference", ObjectSerializer.serialize(rememberFood)).apply();

                } catch (IOException e) {

                    e.printStackTrace();

                }

            }
        });




        ArrayList<String> loadFood = new ArrayList<>();

        try {

            loadFood = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("FoodPreference", ObjectSerializer.serialize(new ArrayList<String>())));

        } catch (IOException e) {

            e.printStackTrace();

        }

        for (String value : loadFood) {
            myListView.setItemChecked(foodOption.indexOf(value), true);
        }

        Log.i("LoadFood", loadFood.toString());




        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }



    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
