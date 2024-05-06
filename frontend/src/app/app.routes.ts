import { Routes } from '@angular/router';
import { BookComponent } from './pages/book/book.component';
import { AuthorComponent } from './pages/author/author.component';

export const routes: Routes = [
    { path: '', redirectTo: 'books', pathMatch: 'full'},
    { path: 'books', component: BookComponent },
    { path: 'authors', component: AuthorComponent },
];
