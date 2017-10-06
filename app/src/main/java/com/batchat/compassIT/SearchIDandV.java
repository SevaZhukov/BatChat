package com.batchat.compassIT;

import android.os.AsyncTask;
import android.util.Log;

import com.batchat.compassIT.Realm.VacancysSkills;
import com.batchat.compassIT.hh.APIService;
import com.batchat.compassIT.hh.GETPage.PageV;
import com.batchat.compassIT.hh.GETVacancy.Vacancy;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.batchat.compassIT.DrawerActivity.mRealm;


/**
 * Created by Севастьян on 29.09.2017.
 */

public class SearchIDandV extends AsyncTask<String, Integer, Void> {

    private APIService service;
    ArrayList<String> masid = new ArrayList<String>();
    private boolean ready = false;
    private int progress_status;
    private Thread thread;

    float percent=0;
    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        progress_status = 0;
    }
    @Override
    protected Void doInBackground(final String... params) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.hh.ru")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(APIService.class);

        for(int i=1; i<60; i++){

            Call<PageV> call = service.getListURL(params[0], i);
            call.enqueue(new Callback<PageV>() {
                @Override
                public void onResponse(Call<PageV> call, Response<PageV> response) {
                    PageV page = response.body();
                    for(int j=0; j<20; j++) {
                        String buf = page.getItems().get(j).getId();
                        String toLow = page.getItems().get(j).getName().toLowerCase();
                        //Log.i("code", "namel: "+ toLow);

                        if(toLow.contains(params[1])||toLow.contains(params[2])){
                            Call<Vacancy> callv = service.getVacancy(buf);
                            final int finalJ = j;
                            callv.enqueue(new Callback<Vacancy>() {
                                @Override
                                public void onResponse(Call<Vacancy> call, Response<Vacancy> response) {

                                    mRealm.beginTransaction();
                                    VacancysSkills vacancysSkills = mRealm.createObject(VacancysSkills.class);

                                    try {
                                        String skills = "";
                                        if(response.body().getKeySkills().size()!=0){
                                            for(int j=0; j<response.body().getKeySkills().size(); j++){
                                                skills+=response.body().getKeySkills().get(j).getName()+",";
                                                if(j==response.body().getKeySkills().size()-1){
                                                    vacancysSkills.setSkills(skills);
                                                    vacancysSkills.setArea(params[1]);
                                                    Log.i("code", "skills: "+skills);
                                                }
                                            }
                                        }
                                    }
                                    catch (Exception e){
                                        Log.i("code", "err " + e);
                                    }
                                    mRealm.commitTransaction();
                                }

                                @Override
                                public void onFailure(Call<Vacancy> call, Throwable t) {
                                    Log.i("code", "SVBID "+ t.toString());
                                }
                            });

                        }
                    }
                }
                @Override
                public void onFailure(Call<PageV> call, Throwable t) {
                    Log.i("code", t.toString());
                }
            });
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void integer) {
        super.onPostExecute(integer);

    }

}