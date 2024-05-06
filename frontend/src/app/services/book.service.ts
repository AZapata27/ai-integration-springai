import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Book } from '../model/book';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class BookService {

  private url: string = `http://localhost:8080/books`;
  private bookChange: Subject<Book[]> = new Subject();

  constructor(private http: HttpClient) { }

  getAll(){
    return this.http.get<Book[]>(this.url);
  }

  getById(id: number){
    return this.http.get<Book>(`${this.url}/${id}`);
  }

  save(book: Book){
    return this.http.post(this.url, book);
  }

  update(id: number, book: Book){
    return this.http.put(`${this.url}/${id}`, book);
  }

  delete(id: number){
    return this.http.delete(`${this.url}/${id}`);
  }

  public setBookChange(books: Book[]){
    this.bookChange.next(books);
  }

  public getBookChange(){
    return this.bookChange.asObservable();
  }
}
