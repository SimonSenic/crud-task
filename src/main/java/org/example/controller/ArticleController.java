package org.example.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.example.model.Article;
import org.example.model.User;
import org.example.repository.ArticleRepository;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/articles")
public class ArticleController {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/add")
    public Article createArticle(@RequestHeader(value="Authorization") String header, @Valid @RequestBody Article article){
        if (articleRepository.findByTitle(article.getTitle()).isPresent())
            throw new IllegalStateException("Article with such title already exists");

        article.setCreatedAt(new Date());
        article.setUser(getUser(header));
        return articleRepository.save(article);
    }

    @PatchMapping("update/{id}")
    public ResponseEntity<Article> updateArticle(@PathVariable(value = "id") int id, @Valid @RequestBody Article body){
        Article article = articleRepository.findById(id).filter(temp -> !temp.isRemoved())
                .orElseThrow(() -> new IllegalArgumentException("404: Not found"));
        article.setTitle(body.getTitle());
        article.setText(body.getText());
        article.setUpdatedAt(new Date());
        articleRepository.save(article);
        return ResponseEntity.ok().body(article);
    }

    @DeleteMapping("remove/{id}") //soft delete
    public ResponseEntity<Article> deleteArticle(@PathVariable(value = "id") int id){
        Article article = articleRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("404: Not found"));
        article.setRemoved(true);
        article.setDeletedAt(new Date());
        articleRepository.save(article);
        return ResponseEntity.ok().body(article);
    }

    @GetMapping("/get")
    public List<Article> getArticles(){
        List<Article> list = articleRepository.findAll();
        list.removeIf(Article::isRemoved);
        return list;
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<Article> getArticleById(@PathVariable(value = "id") int id){
        Article article = articleRepository.findById(id).filter(temp -> !temp.isRemoved())
                .orElseThrow(() -> new IllegalArgumentException("404: Not found"));
        return ResponseEntity.ok().body(article);
    }

    @GetMapping("/removed")
    public List<Article> getRemovedArticles(){
        List<Article> list = articleRepository.findAll();
        list.removeIf(temp -> !temp.isRemoved());
        return list;
    }

    private User getUser(String header){
        Algorithm algorithm = Algorithm.HMAC256("secret".getBytes()); //!!
        String token = header.substring(7);
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(token);
        String username = decodedJWT.getSubject();

        if(!userRepository.findByUsername(username).isPresent())
            throw  new UsernameNotFoundException("User not found");
        return userRepository.findByUsername(username).get();
    }
}
