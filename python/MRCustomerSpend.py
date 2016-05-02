from mrjob.job import MRJob
from mrjob.step import MRStep

class MRCustomerSpend(MRJob):
    def steps(self):
        return [
            MRStep(mapper=self.map_get_cust_amt, reducer=self.reduce_cust_amt),
            MRStep(mapper=self.map_group_amount_cust, reducer=self.reduce_group_amount_cust)
        ]
        
    def map_get_cust_amt(self, _, line):
        (customer, item, amount) = line.split(',')
        yield customer, float(amount)
        
    def reduce_cust_amt(self, customer, amount):
        yield customer, sum(amount)
               
    def map_group_amount_cust(self, customer, amountTotal):
        yield '%04.02f'%float(amountTotal), customer
        
    def reduce_group_amount_cust(self, amountTotal, customerIds):
        for customerId in customerIds:
            yield amountTotal, customerId

        
if __name__ == '__main__':
    MRCustomerSpend.run()