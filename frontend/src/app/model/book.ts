import { Author } from "./author";

export class Book{
    idBook: number;
    name: string;
    review: string;
    author: Author;
    urlCover: string;
    enabled: boolean;
}