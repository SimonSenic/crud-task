package org.example.controller;

import org.example.model.Article;
import org.example.repository.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/articles")
public class ArticleController {

    @Autowired
    private ArticleRepository articleRepository;

    @PostMapping("/add")
    public Article createArticle(@RequestBody Article article){
        article.setCreatedAt(new Date());
        return articleRepository.save(article);
    }

    @PatchMapping("update/{id}")
    public ResponseEntity<Article> updateArticle(@PathVariable(value = "id") int id, @RequestBody Article body){
        Article article = articleRepository.findById(id).filter(temp -> !temp.isRemoved())
                .orElseThrow(() -> new IllegalStateException("404: Not found"));
        article.setTitle(body.getTitle());
        article.setText(body.getText());
        article.setUpdatedAt(new Date());
        articleRepository.save(article);
        return ResponseEntity.ok().body(article);
    }

    @DeleteMapping("remove/{id}") //soft delete
    public ResponseEntity<Article> deleteArticle(@PathVariable(value = "id") int id){
        Article article = articleRepository.findById(id).orElseThrow(() -> new IllegalStateException("404: Not found"));
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
                .orElseThrow(() -> new IllegalStateException("404: Not found"));
        return ResponseEntity.ok().body(article);
    }

    @GetMapping("/removed")
    public List<Article> getRemovedArticles(){
        List<Article> list = articleRepository.findAll();
        list.removeIf(temp -> !temp.isRemoved());
        return list;
    }
}
