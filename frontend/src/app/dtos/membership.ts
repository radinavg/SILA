export class Membership {

  membershipId?: number;
  name: string;
  duration: number;
  minDuration: number;
  price: number;
  applicationUserId?: number;
  studioActivityId?: number;

  constructor(name: string,
              applicationUserId: number,
              studioActivityId: number,
              duration: number,
              minDuration: number,
              price: number) {
    this.name = name;
    this.applicationUserId = applicationUserId;
    this.studioActivityId = studioActivityId;
    this.duration = duration;
    this.minDuration = minDuration;
    this.price = price;
  }
}
