import { HttpClient } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { BookService } from '../../services/book.service';
import { Book } from '../../model/book';
import {MatCardModule} from '@angular/material/card';
import {MatButtonModule} from '@angular/material/button';
import {MatDialog, MatDialogModule} from '@angular/material/dialog';
import { MatToolbarModule } from '@angular/material/toolbar';
import { BookDialogComponent } from './book-dialog/book-dialog.component';

@Component({
  selector: 'app-book',
  standalone: true,
  imports: [MatCardModule, MatButtonModule, MatDialogModule, MatToolbarModule],
  templateUrl: './book.component.html',
  styleUrl: './book.component.css'
})
export class BookComponent implements OnInit{

  books: Book[] = [];

  constructor(
    private bookService: BookService,
    private dialog: MatDialog  
  ){}

  ngOnInit(): void {
      this.bookService.getAll().subscribe(data => this.books = data);
      this.bookService.getBookChange().subscribe(data => this.books = data);
  }

  openDialog(){
    this.dialog.open(BookDialogComponent, {
      width: '1024px',
      //data: {}
    });
  }
}
