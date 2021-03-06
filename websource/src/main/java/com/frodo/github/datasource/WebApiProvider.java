package com.frodo.github.datasource;


import com.frodo.github.bean.dto.response.Repo;
import com.frodo.github.bean.dto.response.User;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuwei19 on 2016/5/14.
 */
public class WebApiProvider {
    private String host;
    private String userAgent;

    public WebApiProvider(String host, String userAgent) {
        this.host = host;
        this.userAgent = userAgent;
    }

    public User getUser(String username) throws IOException {
        return fetch(host, username, userAgent);
    }

    private static User fetch(String host, String user, String userAgent) throws IOException {
        User userWebInfo = null;
        Document doc = Jsoup.connect(host + '/' + user).userAgent(userAgent).get();
        Elements popularReposElems = doc.select("div[class=bubble ]");
        String starred = doc.select("div[class=vcard-stat]").get(1).select("strong[class=vcard-stat-count]").text();
        if (popularReposElems != null) {
            userWebInfo = new User();
            if (starred != null) {
                if (starred.contains("k")) {
                    userWebInfo.starred = Integer.valueOf(starred.replace("[.k]", ""));
                } else {
                    userWebInfo.starred = Integer.valueOf(starred);
                }
            }
            for (int i = 0; i < popularReposElems.size(); i++) {
                Element element = popularReposElems.get(i);
                if (element != null) {
                    Elements repoElems = element.select("a[class=list-item repo-list-item]");
                    if (repoElems != null) {
                        List<Repo> repos = new ArrayList<>(repoElems.size());
                        for (int j = 0; j < repoElems.size(); j++) {
                            Element elementRepo = repoElems.get(j);
                            if (elementRepo != null) {
                                Repo repo = new Repo();
                                repo.archive_url = elementRepo.attr("href");

                                Elements elementRepoString = elementRepo.select("div[class=list-item-title repo-name]");
                                String repoFullName = elementRepoString.get(0).text();

                                if (repoFullName.contains("/")) {
                                    String[] strings = repoFullName.split("/");
                                    User owner = new User();
                                    owner.login = strings[0];
                                    repo.owner = owner;
                                    repo.name = strings[1];
                                } else {
                                    repo.name = repoFullName;
                                }

                                Elements elementStars = elementRepo.select("strong[class=meta]");
                                repo.stargazers_count = Integer.parseInt(elementStars.get(0).text().replace(",", ""));
                                repos.add(repo);
                            }
                        }
                        if (i == 0) {
                            userWebInfo.popularRepositories = repos;
                        } else if (i == 1) {
                            userWebInfo.contributeToRepositories = repos;
                        }
                    }
                }
            }
        }
        return userWebInfo;
    }
}
