import {StudioActivity} from "./studioActivity";
import {ProfileImageDto} from "./image";

export class Instructor {
  id: number;
  firstName: string;
  lastName: string;
  studioActivities: StudioActivity[]
  profileImage: ProfileImageDto
}


export class InstructorCreateDto {

  firstName: string;
  lastName: string;
  profileImage: File
}
