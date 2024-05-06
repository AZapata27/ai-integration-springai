import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Author } from '../model/author';

@Injectable({
  providedIn: 'root'
})
export class AuthorService {

  private url: string = `http://localhost:8080/authors`;

  constructor(private http: HttpClient) { }

  getAll(){
    return this.http.get<Author[]>(this.url);
  }

  getById(id: number){
    return this.http.get<Author>(`${this.url}/${id}`);
  }

  save(author: Author){
    return this.http.post(this.url, author);
  }

  update(id: number, author: Author){
    return this.http.put(`${this.url}/${id}`, author);
  }

  delete(id: number){
    return this.http.delete(`${this.url}/${id}`);
  }
}
